// Crie um projeto separado para testar se seu JAR funciona
// 1. Adicione seu JAR como dependência
// 2. Teste se consegue importar e usar sua classe:
import br.edu.icev.aed.forense.MinhaAnaliseForense;
import br.edu.icev.aed.forense.AnaliseForenseAvancada;
import br.edu.icev.aed.forense.Alerta;



import java.util.*;

public class TesteValidacao {
    public static void main(String[] args) {
        AnaliseForenseAvancada impl = new MinhaAnaliseForense();
        
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("        🔍 BATERIA DE TESTES - ANÁLISE FORENSE           ");
        System.out.println("═══════════════════════════════════════════════════════════\n");
        
        try {
            testarDesafio1(impl);
            testarDesafio2(impl);
            testarDesafio3(impl);
            testarDesafio4(impl);
            testarDesafio5(impl);
            
            System.out.println("\n═══════════════════════════════════════════════════════════");
            System.out.println("          ✅ TODOS OS TESTES CONCLUÍDOS COM SUCESSO!        ");
            System.out.println("═══════════════════════════════════════════════════════════");
            
        } catch (Exception e) {
            System.err.println("\n❌ ERRO DURANTE OS TESTES:");
            e.printStackTrace();
        }
    }
    
    // ========== DESAFIO 1: SESSÕES INVÁLIDAS ==========
    private static void testarDesafio1(AnaliseForenseAvancada impl) throws Exception {
        System.out.println("┌─────────────────────────────────────────────────────────┐");
        System.out.println("│  📋 DESAFIO 1: Encontrar Sessões Inválidas (Pilha)     │");
        System.out.println("└─────────────────────────────────────────────────────────┘");
        
        Set<String> invalidas = impl.encontrarSessoesInvalidas("test_sessoes_invalida.csv");
        
        System.out.println("✅ Teste executado com sucesso!");
        System.out.println("   Sessões inválidas encontradas: " + invalidas.size());
        System.out.println("   Sessões: " + invalidas);
        
        // Validação básica
        if (invalidas.isEmpty()) {
            System.out.println("   ⚠️  Atenção: Nenhuma sessão inválida encontrada");
        }
        
        System.out.println();
    }
    
    // ========== DESAFIO 2: RECONSTRUIR LINHA DO TEMPO ==========
    private static void testarDesafio2(AnaliseForenseAvancada impl) throws Exception {
        System.out.println("┌─────────────────────────────────────────────────────────┐");
        System.out.println("│  📋 DESAFIO 2: Reconstruir Linha do Tempo (Fila)       │");
        System.out.println("└─────────────────────────────────────────────────────────┘");
        
        // Teste 1: Sessão existente
        System.out.println("🔍 Teste 2.1: session-alpha-723");
        List<String> timeline1 = impl.reconstruirLinhaTempo("teste_timeline.csv", "session-alpha-723");
        System.out.println("   ✅ Ações encontradas: " + timeline1.size());
        System.out.println("   Timeline: " + timeline1);
        
        // Teste 2: Sessão inexistente
        System.out.println("\n🔍 Teste 2.2: sessão inexistente");
        List<String> timeline2 = impl.reconstruirLinhaTempo("teste_timeline.csv", "session-nao-existe");
        System.out.println("   ✅ Ações encontradas: " + timeline2.size());
        
        if (timeline2.isEmpty()) {
            System.out.println("   ✅ Correto: Lista vazia para sessão inexistente");
        } else {
            System.out.println("   ❌ ERRO: Deveria retornar lista vazia!");
        }
        
        System.out.println();
    }
    
    // ========== DESAFIO 3: PRIORIZAR ALERTAS ==========
    private static void testarDesafio3(AnaliseForenseAvancada impl) throws Exception {
        System.out.println("┌─────────────────────────────────────────────────────────┐");
        System.out.println("│  📋 DESAFIO 3: Priorizar Alertas (PriorityQueue)       │");
        System.out.println("└─────────────────────────────────────────────────────────┘");
        
        // Teste 1: Top 5 alertas
        System.out.println("🔍 Teste 3.1: Top 5 Alertas Mais Críticos");
        List<Alerta> top5 = impl.priorizarAlertas("test_alertas.csv", 5);
        System.out.println("   ✅ Alertas retornados: " + top5.size());
        
        for (int i = 0; i < Math.min(5, top5.size()); i++) {
            Alerta a = top5.get(i);
            System.out.printf("   %d. [Severidade: %d] %s → %s\n", 
                i + 1, 
                a.getSeverityLevel(), 
                a.getActionType(), 
                a.getTargetResource()
            );
        }
        
        // Validar ordem decrescente
        boolean ordenado = true;
        for (int i = 0; i < top5.size() - 1; i++) {
            if (top5.get(i).getSeverityLevel() < top5.get(i + 1).getSeverityLevel()) {
                ordenado = false;
                break;
            }
        }
        
        if (ordenado) {
            System.out.println("   ✅ Alertas estão em ordem decrescente de severidade");
        } else {
            System.out.println("   ❌ ERRO: Alertas NÃO estão ordenados!");
        }
        
        // Teste 2: N = 0
        System.out.println("\n🔍 Teste 3.2: N = 0");
        List<Alerta> top0 = impl.priorizarAlertas("test_alertas.csv", 0);
        System.out.println("   ✅ Alertas retornados: " + top0.size());
        
        if (top0.isEmpty()) {
            System.out.println("   ✅ Correto: Lista vazia para N = 0");
        } else {
            System.out.println("   ❌ ERRO: Deveria retornar lista vazia!");
        }
        
        System.out.println();
    }
    
    // ========== DESAFIO 4: PICOS DE TRANSFERÊNCIA ==========
    private static void testarDesafio4(AnaliseForenseAvancada impl) throws Exception {
        System.out.println("┌─────────────────────────────────────────────────────────┐");
        System.out.println("│  📋 DESAFIO 4: Picos de Transferência (Stack-NGE)      │");
        System.out.println("└─────────────────────────────────────────────────────────┘");
        
        Map<Long, Long> picos = impl.encontrarPicosTransferencia("teste_picos.csv");
        
        System.out.println("✅ Teste executado com sucesso!");
        System.out.println("   Picos encontrados: " + picos.size());
        
        if (picos.isEmpty()) {
            System.out.println("   ⚠️  Nenhum pico de transferência detectado");
        } else {
            System.out.println("   Primeiros 5 picos:");
            int count = 0;
            for (Map.Entry<Long, Long> entry : picos.entrySet()) {
                if (count++ >= 5) break;
                System.out.printf("   Timestamp %d → Próximo maior: %d\n", 
                    entry.getKey(), entry.getValue());
            }
            
            if (picos.size() > 5) {
                System.out.println("   ... e mais " + (picos.size() - 5) + " picos");
            }
        }
        
        System.out.println();
    }
    
    // ========== DESAFIO 5: RASTREAR CONTAMINAÇÃO ==========
    private static void testarDesafio5(AnaliseForenseAvancada impl) throws Exception {
        System.out.println("┌─────────────────────────────────────────────────────────┐");
        System.out.println("│  📋 DESAFIO 5: Rastrear Contaminação (BFS - Grafo)     │");
        System.out.println("└─────────────────────────────────────────────────────────┘");
        
        // Teste 1: Caminho direto
        System.out.println("🔍 Teste 5.1: /usr/bin/sshd → /etc/shadow");
        Optional<List<String>> path1 = impl.rastrearContaminacao(
            "test_contaminacao.csv", "/usr/bin/sshd", "/etc/shadow"
        );
        
        if (path1.isPresent()) {
            System.out.println("   ✅ Caminho encontrado!");
            System.out.println("   Tamanho: " + path1.get().size() + " recursos");
            System.out.println("   Caminho: " + String.join(" → ", path1.get()));
            
            // Validar início e fim
            if (path1.get().get(0).equals("/usr/bin/sshd") && 
                path1.get().get(path1.get().size() - 1).equals("/etc/shadow")) {
                System.out.println("   ✅ Início e fim corretos");
            } else {
                System.out.println("   ❌ ERRO: Início ou fim incorretos!");
            }
        } else {
            System.out.println("   ❌ Caminho NÃO encontrado (deveria existir!)");
        }
        
        // Teste 2: Caminho através de intermediário
        System.out.println("\n🔍 Teste 5.2: /usr/bin/sshd → 192.168.1.100");
        Optional<List<String>> path2 = impl.rastrearContaminacao(
            "test_contaminacao.csv", "/usr/bin/sshd", "192.168.1.100"
        );
        
        if (path2.isPresent()) {
            System.out.println("   ✅ Caminho encontrado!");
            System.out.println("   Tamanho: " + path2.get().size() + " recursos");
            System.out.println("   Caminho: " + String.join(" → ", path2.get()));
        } else {
            System.out.println("   ❌ Caminho NÃO encontrado (deveria existir!)");
        }
        
        // Teste 3: Caminho inexistente (grafo direcionado)
        System.out.println("\n🔍 Teste 5.3: /etc/shadow → /usr/bin/sshd (direção reversa)");
        Optional<List<String>> path3 = impl.rastrearContaminacao(
            "test_contaminacao.csv", "/etc/shadow", "/usr/bin/sshd"
        );
        
        if (path3.isPresent()) {
            System.out.println("   ❌ ERRO: Não deveria encontrar caminho (grafo direcionado)!");
        } else {
            System.out.println("   ✅ Correto! Caminho não existe (Optional.empty())");
        }
        
        // Teste 4: Origem = Destino
        System.out.println("\n🔍 Teste 5.4: /usr/bin/sshd → /usr/bin/sshd (mesmo recurso)");
        Optional<List<String>> path4 = impl.rastrearContaminacao(
            "test_contaminacao.csv", "/usr/bin/sshd", "/usr/bin/sshd"
        );
        
        if (path4.isPresent()) {
            System.out.println("   ✅ Caminho encontrado!");
            System.out.println("   Tamanho: " + path4.get().size());
            
            if (path4.get().size() == 1 && path4.get().get(0).equals("/usr/bin/sshd")) {
                System.out.println("   ✅ Correto: Lista com 1 elemento");
            } else {
                System.out.println("   ❌ ERRO: Deveria ter apenas 1 elemento!");
            }
        } else {
            System.out.println("   ⚠️  Retornou Optional.empty() (aceitável)");
        }
        
        // Teste 5: Recursos desconectados
        System.out.println("\n🔍 Teste 5.5: /usr/bin/sshd → /opt/app/server (sessões separadas)");
        Optional<List<String>> path5 = impl.rastrearContaminacao(
            "test_contaminacao.csv", "/usr/bin/sshd", "/opt/app/server"
        );
        
        if (path5.isPresent()) {
            System.out.println("   ❌ ERRO: Recursos não deveriam estar conectados!");
        } else {
            System.out.println("   ✅ Correto! Recursos desconectados (Optional.empty())");
        }
        
        // Teste 6: Caminho mais longo
        System.out.println("\n🔍 Teste 5.6: /bin/ls → /var/log/auth.log");
        Optional<List<String>> path6 = impl.rastrearContaminacao(
            "test_contaminacao.csv", "/bin/ls", "/var/log/auth.log"
        );
        
        if (path6.isPresent()) {
            System.out.println("   ✅ Caminho encontrado!");
            System.out.println("   Tamanho: " + path6.get().size() + " recursos");
            System.out.println("   Caminho: " + String.join(" → ", path6.get()));
        } else {
            System.out.println("   ❌ Caminho NÃO encontrado");
        }
        
        System.out.println();
    }
}
