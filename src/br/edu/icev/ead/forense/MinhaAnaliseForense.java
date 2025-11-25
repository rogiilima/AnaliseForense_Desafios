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
    public Map<Long, Long> encontrarPicosTransferencia(String caminhoArquivo) 
            throws IOException {
        // TODO: Implementar Desafio 4
        return new HashMap<>();
    }
    
    @Override
    public Optional<List<String>> rastrearContaminacao(
            String caminhoArquivo, String recursoInicial, String recursoAlvo) 
            throws IOException {
        // TODO: Implementar Desafio 5
        return Optional.empty();
    }
}
