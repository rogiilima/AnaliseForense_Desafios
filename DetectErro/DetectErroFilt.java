package DetectErro;

import java.io.*;
import java.util.*;

public class DetectErroFilt {

    public static Map<Long, Long> encontrarPicosTransferencia(String caminhoArquivo) throws IOException {


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

        return resposta;
    }
}
