package br.edu.icev.ead.forense;
import br.edu.icev.aed.forense.AnaliseForenseAvancada;
import br.edu.icev.aed.forense.Alerta;

import java.util.*;
import java.io.*;


public class MinhaAnaliseForense implements AnaliseForenseAvancada {
    public MinhaAnaliseForense() {
    }
    
    @Override
    public Set<String> encontrarSessoesInvalidas(String caminhoArquivo) throws IOException {
        Map<String, Deque<String>> mapDeLoginUsuarios = new HashMap<>();
        Set<String> sessoesInvalidas = new HashSet<>();
        
        try (BufferedReader reader = new BufferedReader(
                new FileReader(caminhoArquivo), 65536)) { // o segundo parametro do FileReader(65536) é para otimização, visto que ele cria um buffer temp de 64KB na RAM para as linhas de logs
            
            reader.readLine(); // pula direto a primeira linha
            
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] campos = linha.split(",", 7); // esse 7 serve também para otimizar a leitura, visto que ele vai criar um vetor so com 7 posições
                
                String userId = campos[1];
                String sessionId = campos[2];
                String action = campos[3];
                
                //Cria a pilha de sessao do usurio, se não existir no Map. Ou retorna se já exite
                Deque<String> pilha = mapDeLoginUsuarios.computeIfAbsent(userId, pilhaSessoes -> new ArrayDeque<>());
                
                //Logica principal
                if ("LOGIN".equals(action)) {
                    //Verifica se eh um login aninhado
                    if (!pilha.isEmpty()) {
                        sessoesInvalidas.add(pilha.peek());//adiciona no set a sessao antes dessa que fez login
                    }
                    //adiciona a sessão na pilha se for normal ou com login irregular
                    pilha.push(sessionId);
                    
                } 
                if ("LOGOUT".equals(action)) {
                    //Verifica se a pilha de sessoes essta vazia ou se a sessao que fez logout eh diferente da ultima de login
                    if (pilha.isEmpty() || !sessionId.equals(pilha.peek())) {
                        sessoesInvalidas.add(sessionId);
                    } else {
                        pilha.pop();//desempilha a sessão atual se tudo corresponder
                    }
                }
            }
        }
        
        // Adiciona sessoes que so fizeram LOGIN
        for (Deque<String> pilha : mapDeLoginUsuarios.values()) {
            sessoesInvalidas.addAll(pilha);
        }
        
        return sessoesInvalidas;
    }
    
    @Override
    public List<String> reconstruirLinhaTempo(String caminhoArquivo, String sessionId) 
            throws IOException {
        List<String> timeline = new ArrayList<>();
    
        //Como o log já está em ordem cronologica, é só fazer a leitura sequencial
        //O leitor do csv aqui é implementado da mesma lógica que no encontrarSessoesInvalidas( eu só apaguei os comentarios)
        try (BufferedReader reader = new BufferedReader(
                new FileReader(caminhoArquivo), 65536)) {
            
            reader.readLine();
            
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] campos = linha.split(",", 7);
                
                String sId = campos[2]; // sessionsId    
                String action = campos[3];   
                
                // Se for a sessão que procuramos
                if (sessionId.equals(sId)) {
                    timeline.add(action);
                }
            }
        }
        
        return timeline;
    }
    
    @Override
    public List<Alerta> priorizarAlertas(String caminhoArquivo, int n) 
            throws IOException {//caso o numero de alertas for 0
        if (n <= 0) {
            return new ArrayList<>();
        }

        PriorityQueue<Alerta> alertasPrioridades = new PriorityQueue<>(
                (a, b) -> Integer.compare(b.getSeverityLevel(), a.getSeverityLevel())); // Compara em ordem DECRESCENTE

        //O leitor do csv aqui é implementado da mesma lógica que no encontrarSessoesInvalidas( eu só apaguei os comentarios)
        try (BufferedReader reader = new BufferedReader(
                new FileReader(caminhoArquivo), 65536)) {

            reader.readLine();

            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] campos = linha.split(",", 7);

                long timestamp = Long.parseLong(campos[0]);
                String userId = campos[1];
                String sessionId = campos[2];
                String actionType = campos[3];
                String targetResource = campos[4];
                int severityLevel = Integer.parseInt(campos[5]);

                // Lógica Principal
                //Essa parte pega o BYTES_TRANSFERRED
                long bytesTransferred = 0;
                if (campos.length > 6 && !campos[6].isEmpty()) {
                    try {
                        bytesTransferred = Long.parseLong(campos[6]);
                    } catch (NumberFormatException e) {
                        // Ignora se não for número válido
                        bytesTransferred = 0;
                    }
                }
                // Criar e adicionar alerta
                Alerta alerta = new Alerta(
                        timestamp, userId, sessionId, actionType,
                        targetResource, severityLevel, bytesTransferred
                );

                alertasPrioridades.offer(alerta);


            }
        }

        // Extrair os N alertas mais críticos
        List<Alerta> topAlertas = new ArrayList<>();
        for (int i = 0; i < n && !alertasPrioridades.isEmpty(); i++) {
            topAlertas.add(alertasPrioridades.poll());
        }

        return topAlertas;
    }
        
    @Override
    public  Map<Long, Long> encontrarPicosTransferencia(String caminhoArquivo) throws IOException {


        List<Long> listaTimestamps = new ArrayList<>();
        List<Long> listaBytes = new ArrayList<>();

        try (BufferedReader leitor = new BufferedReader(new FileReader(caminhoArquivo))) {
            String linha;
            boolean cabecalho = true;

            while ((linha = leitor.readLine()) != null) {
                if (cabecalho) {
                    cabecalho = false;
                    continue;
                }

                String[] partes = linha.split(",");

                if (partes.length >= 2) {
                    try {
                        long time = Long.parseLong(partes[0].trim());
                        long bytes = Long.parseLong(partes[1].trim());

                        listaTimestamps.add(time);
                        listaBytes.add(bytes);

                    } catch (NumberFormatException ignore) {
                        // Ignorar linha inválida
                    }
                }
            }
        }

        // Chamar o detector real
        return detectarProximoMaior(listaTimestamps, listaBytes);
    }


    private static Map<Long, Long> detectarProximoMaior(List<Long> tempos, List<Long> bytes) {

        Map<Long, Long> resposta = new HashMap<>();
        Stack<Integer> pilha = new Stack<>();

        int tamanho = tempos.size();

        // Percorrer ao contrário, conforme a regra
        for (int pos = tamanho - 1; pos >= 0; pos--) {

            long atualBytes = bytes.get(pos);

            // Remover todos que são <= ao atual
            while (!pilha.isEmpty() && bytes.get(pilha.peek()) <= atualBytes) {
                pilha.pop();
            }

            // Se sobrou alguém na pilha → esse é o próximo maior
            if (!pilha.isEmpty()) {
                int indiceMaior = pilha.peek();
                resposta.put(tempos.get(pos), tempos.get(indiceMaior));
            }

            // Empilhar este índice
            pilha.push(pos);
        }
//
        return resposta;
    }
    
    @Override
    public Optional<List<String>> rastrearContaminacao(
            String caminhoArquivo, String recursoInicial, String recursoAlvo) 
            throws IOException {
        //Construcao do grafo
        Map<String, List<String>> grafo = construirGrafo(caminhoArquivo);
        
        //Se a origem for igual ao destino
        if (recursoInicial.equals(recursoAlvo)) {
            if (grafo.containsKey(recursoInicial)) {
                return Optional.of(Collections.singletonList(recursoInicial));
            } else {
                return Optional.empty();
            }
        }
        
        //retorna o caminho
        return bfs(grafo, recursoInicial, recursoAlvo);
    }

    private Map<String, List<String>> construirGrafo(String caminhoArquivo) throws IOException {
        Map<String, List<String>> grafo = new HashMap<>();
        
        // Agrupa eventos por sessão (como o invasor percorreu o grafo naquela sessão)
        Map<String, List<String>> eventosPorSessao = new HashMap<>();
        
        try (BufferedReader reader = new BufferedReader(
                new FileReader(caminhoArquivo), 65536)) {
            
            reader.readLine(); // pula header
            
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] campos = linha.split(",", 7);
                
                if (campos.length < 5) continue;// Isso aqui serve para verificar se o csv tem menos campos do que o que eu vou precisa
                
                String sessionId = campos[2];
                String targetResource = campos[4];
                
                // Adicionar recurso à lista da sessão
                eventosPorSessao.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(targetResource);
            }
        }
        
        // Algoritmo para construir as arestas
        for (List<String> recursos : eventosPorSessao.values()) {
            for (int i = 0; i < recursos.size() - 1; i++) {
                String origem = recursos.get(i);
                String destino = recursos.get(i + 1);
                
                // Adicionar aresta ao grafo
                grafo.computeIfAbsent(origem, k -> new ArrayList<>()).add(destino);
            }
        }
        
        return grafo;
    }

    private Optional<List<String>> bfs(Map<String, List<String>> grafo, String inicio, String alvo) {
        
        // Verificar se início existe no grafo
        if (!grafo.containsKey(inicio)) {
            return Optional.empty();
        }
        
        // Fila do BFS
        Queue<String> fila = new LinkedList<>();
        fila.offer(inicio);
        
        // Conjunto de visitados
        Set<String> visitados = new HashSet<>();
        visitados.add(inicio);
        
        // Mapa dos pais (para reconstruir o caminho)
        Map<String, String> pai = new HashMap<>();
        pai.put(inicio, null);
        
        // BFS
        while (!fila.isEmpty()) {
            String atual = fila.poll();
            
            // Verifica se é o alvo
            if (atual.equals(alvo)) {
                return Optional.of(reconstruirCaminho(pai, inicio, alvo));
            }
            
            // Explorar vizinhos
            List<String> vizinhos = grafo.getOrDefault(atual, Collections.emptyList());
            for (String vizinho : vizinhos) {
                if (!visitados.contains(vizinho)) {
                    visitados.add(vizinho);
                    pai.put(vizinho, atual);
                    fila.offer(vizinho);
                }
            }
        }
        
        // Quando não encontra caminho
        return Optional.empty();
    }

    private List<String> reconstruirCaminho(Map<String, String> predecessor, String inicio, String alvo) {
        List<String> caminho = new ArrayList<>();
        String atual = alvo;
        
        // Reconstroe caminho de trás para frente
        while (atual != null) {
            caminho.add(atual);
            atual = predecessor.get(atual);
        }
        
        // Inverte para ter início → alvo
        Collections.reverse(caminho);
        
        return caminho;
    }
}
