package DetectErro;
import java.io.*;
import java.util.*;

public class DetectorErroFilt {


    public static Map<Long, Long> encontrarPicosTransferencia(String caminhoArquivo) throws IOException {
        List<Long> timestamps = new ArrayList<>();
        List<Long> bytesList = new ArrayList<>();

        // Ler o arquivo CSV e armazenar apenas timestamps e bytes em listas
        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo))) {
            String linha;
            boolean primeiraLinha = true; // Assumindo que a primeira linha é cabeçalho
            while ((linha = br.readLine()) != null) {
                if (primeiraLinha) {
                    primeiraLinha = false; // Pular cabeçalho
                    continue;
                }
                String[] partes = linha.split(","); // Assumindo formato: timestamp,bytesTransferred
                if (partes.length >= 2) {
                    try {
                        long timestamp = Long.parseLong(partes[0].trim());
                        long bytes = Long.parseLong(partes[1].trim());
                        timestamps.add(timestamp);
                        bytesList.add(bytes);
                    } catch (NumberFormatException e) {
                        // Ignorar linhas inválidas
                    }
                }
            }
        }

        // Assumir que o CSV já está ordenado por timestamp crescente (comum em logs)
        // Se não estiver, descomente a linha abaixo, mas isso aumenta complexidade para O(n log n)
        // Collections.sort(timestamps); // Mas bytesList precisaria ser ordenada junto, complicando

        // Chamar o método de detecção de picos
        return findTransferPeaks(timestamps, bytesList);
    }


    private static Map<Long, Long> findTransferPeaks(List<Long> timestamps, List<Long> bytesList) {
        Map<Long, Long> result = new HashMap<>();
        Stack<Integer> stack = new Stack<>(); // Stack de índices para as listas

        int n = timestamps.size();
        // Iterar pelos logs em ordem cronológica inversa (do fim para o começo)
        for (int i = n - 1; i >= 0; i--) {
            long currentBytes = bytesList.get(i);

            // Enquanto a pilha não estiver vazia e bytes do topo <= currentBytes, desempilhe
            while (!stack.isEmpty() && bytesList.get(stack.peek()) <= currentBytes) {
                stack.pop();
            }

            // Se a pilha não estiver vazia, o evento no topo é o "próximo elemento maior"
            // Adicione (timestamps[i], timestamps[topo]) ao mapa
            if (!stack.isEmpty()) {
                result.put(timestamps.get(i), timestamps.get(stack.peek()));
            }

            // Empilhe o índice i
            stack.push(i);
        }

        return result;
    }

}
