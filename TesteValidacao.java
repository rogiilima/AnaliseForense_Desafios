// Crie um projeto separado para testar se seu JAR funciona
// 1. Adicione seu JAR como dependência
// 2. Teste se consegue importar e usar sua classe:
import br.edu.icev.aed.forense.AnaliseForenseAvancada;
import br.edu.icev.ead.forense.MinhaAnaliseForense;

import java.util.List;
import java.util.Set;

public class TesteValidacao {
    public static void main(String[] args) throws Exception {
        //Verifica qual versao da jdk o vscode está rodando
        System.out.println("Rodando com Java: " + System.getProperty("java.version"));
        
        // Simula o que o validador fará
        AnaliseForenseAvancada impl = new MinhaAnaliseForense();
        
        Set<String> invalidasaa = impl.encontrarSessoesInvalidas("test_sessoes_invalidas.csv");
        System.out.println("Encontradas: " + invalidasaa.size() + " sessões inválidas");
        System.out.println("===============");
        for (String i : invalidasaa) {
            System.out.println(i);
        }

        // ========== TESTE DESAFIO 1 ==========
        System.out.println("📋 DESAFIO 1: Sessões Inválidas - Com csv de 1000_linhas");
        System.out.println("===============================");
        Set<String> invalidas = impl.encontrarSessoesInvalidas("desafio1_1005_linhas.csv");
        System.out.println("✅ Encontradas: " + invalidas.size() + " sessões inválidas");
        System.out.println("Sessões: " + invalidas);
        
        // ========== TESTE DESAFIO 2 ==========
        System.out.println("\n📋 DESAFIO 2: Reconstruir Linha do Tempo");
        System.out.println("=========================================");
        
        // Teste 1: Sessão válida completa
        System.out.println("\n🔍 Testando session-alpha-723:");
        List<String> timeline1 = impl.reconstruirLinhaTempo("teste_timeline.csv", "session-alpha-723");
        System.out.println("✅ Ações encontradas: " + timeline1.size());
        System.out.println("Timeline:");
        for (int i = 0; i < timeline1.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + timeline1.get(i));
        }
        
        // Teste 2: Outra sessão
        System.out.println("\n🔍 Testando session-beta-112:");
        List<String> timeline2 = impl.reconstruirLinhaTempo("teste_timeline.csv", "session-beta-112");
        System.out.println("✅ Ações encontradas: " + timeline2.size());
        System.out.println("Timeline:");
        for (int i = 0; i < timeline2.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + timeline2.get(i));
        }
        
        // Teste 3: Sessão inexistente
        System.out.println("\n🔍 Testando sessão inexistente:");
        List<String> timeline3 = impl.reconstruirLinhaTempo("teste_timeline.csv", "session-nao-existe");
        System.out.println("✅ Ações encontradas: " + timeline3.size());
        System.out.println("Lista vazia: " + timeline3.isEmpty());
        
        System.out.println("\n✅ Todos os testes concluídos!");


    }
}
