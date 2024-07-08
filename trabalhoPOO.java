import java.util.Scanner;
import java.util.Set;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.ExecutorService;

public class trabalhoPOO {

    public static void main(String[] args) {
        Agencia agencia = new Agencia();

        String arquivoHoteis = "C:\\Users\\rafin\\Desktop\\TrabalhoPOO\\formato-hoteis.csv";
        lerHoteisDeArquivo(arquivoHoteis, agencia);

        String arquivoVoos = "C:\\Users\\rafin\\Desktop\\TrabalhoPOO\\formato-voos.csv";
        lerVoosDeArquivo(arquivoVoos, agencia);

        List<Usuario> usuarios = new ArrayList<>();
        String arquivoUsuarios = "C:\\Users\\rafin\\Desktop\\TrabalhoPOO\\formato-clientes.csv";
        lerUsuariosDeArquivo(arquivoUsuarios, usuarios);

        System.out.println("Iniciando processamento sequencial...");
        long startTimeSeq = System.nanoTime();
        agencia.processarUsuariosSequencialmente(usuarios);
        long endTimeSeq = System.nanoTime();
        long durationSeq = TimeUnit.NANOSECONDS.toMillis(endTimeSeq - startTimeSeq);
        System.out.println("Tempo de processamento sequencial: " + durationSeq + " ms");

        gerarArquivoSaida(usuarios, "saida-sequencial.csv");
        
        System.out.println("Iniciando processamento paralelo...");
        long startTimePar = System.nanoTime();
        agencia.processarUsuariosParalelamente(usuarios);
        long endTimePar = System.nanoTime();
        long durationPar = TimeUnit.NANOSECONDS.toMillis(endTimePar - startTimePar);
        System.out.println("Tempo de processamento paralelo: " + durationPar + " ms");

        gerarArquivoSaida(usuarios, "saida-paralela.csv");
    }

    public static void lerHoteisDeArquivo(String nomeArquivo, Agencia agencia) {
        try (BufferedReader br = new BufferedReader(new FileReader(nomeArquivo))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] dados = linha.split(";");
                if (dados.length == 5) {
                    String nome = dados[1];
                    String localizacao = dados[0];
                    int estrelas = extrairNumero(dados[4]);
                    double diaria = Double.parseDouble(dados[3].replace("R$ ", ""));
                    int quartosDisponiveis = Integer.parseInt(dados[2].split(" ")[0]);

                    Hotel hotel = new Hotel(nome, localizacao, estrelas, diaria, quartosDisponiveis);
                    agencia.adicionarHotel(hotel);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void lerVoosDeArquivo(String nomeArquivo, Agencia agencia) {
        try (BufferedReader br = new BufferedReader(new FileReader(nomeArquivo))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] dados = linha.split(";");
                if (dados.length == 6) {
                    String origem = dados[0];
                    String destino = dados[1];
                    String data = dados[2];
                    String hora = dados[3];
                    int assentosDisponiveis = extrairNumero(dados[4]);
                    double preco = Double.parseDouble(dados[5].replace("R$ ", ""));

                    Voo voo = new Voo(origem, destino, data, hora, assentosDisponiveis, preco);
                    agencia.adicionarVoo(voo);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void lerUsuariosDeArquivo(String nomeArquivo, List<Usuario> usuarios) {
        try (BufferedReader br = new BufferedReader(new FileReader(nomeArquivo))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] dados = linha.split(";");
                if (dados.length == 6) {
                    String nome = dados[0];
                    String origem = dados[1];
                    String destino = dados[2];
                    int qtdDiarias = extrairNumero(dados[3]);
                    int preferenciaEstrelasHotel = extrairNumero(dados[4]);
                    double saldo = Double.parseDouble(dados[5].replace("R$ ", ""));

                    Usuario usuario = new Usuario(nome, origem, destino, qtdDiarias, preferenciaEstrelasHotel, saldo);
                    usuarios.add(usuario);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int extrairNumero(String texto) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(texto);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        } else {
            throw new NumberFormatException("Nenhum número encontrado na string: " + texto);
        }
    }

     public static void gerarArquivoSaida(List<Usuario> usuarios, String nomeArquivo) {
        int totalPedidos = usuarios.size();
        Set<String> clientesDistintos = new HashSet<>();
        int pedidosAtendidos = 0;
        double valorTotalGasto = 0;
        double valorTotalHoteis = 0;
        double valorTotalVoos = 0;

        for (Usuario usuario : usuarios) {
            clientesDistintos.add(usuario.getNome());
            if (usuario.isOrcamentoAceito()) {
                pedidosAtendidos++;
                Orcamento orcamento = usuario.getOrcamento();
                valorTotalGasto += orcamento.getCustoTotal();
                valorTotalHoteis += orcamento.getHotel().getDiaria() * usuario.getQtdDiarias();
                for (Voo voo : orcamento.getVoos()) {
                    valorTotalVoos += voo.getPreco();
                }
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(nomeArquivo))) {
            writer.write(totalPedidos + ";" +
                    clientesDistintos.size() + ";" +
                    pedidosAtendidos + ";" +
                    valorTotalGasto + ";" +
                    valorTotalHoteis + ";" +
                    valorTotalVoos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Usuario {
    private String nome;
    private String email;
    private String origem;
    private int preferenciaEstrelasHotel;
    private String destino;
    private Orcamento orcamento;
    private boolean orcamentoAceito;
    private int qtdDiarias; 
    private double saldo;
    private int tempoAvaliacao;
    
    public Usuario(){

    }

    public Usuario(String nome, String origem, String destino, int qtdDiarias, int preferenciaEstrelasHotel, double saldo) {
        this.nome = nome;
        this.origem = origem;
        this.destino = destino;
        this.qtdDiarias = qtdDiarias;
        this.saldo = saldo;
        this.preferenciaEstrelasHotel = preferenciaEstrelasHotel;
    }

    

    public void avaliarOrcamento(Orcamento orcamento) {
        this.orcamento = orcamento;
    }

    public void aceitarOrcamento() {
        this.orcamentoAceito = true;
        //System.out.println(nome + " aceitou o orçamento.");
        if (orcamento != null) {
            for (Voo voo : orcamento.getVoos()) {
                voo.reservarAssento();
            }
            orcamento.getHotel().reservarQuarto();
        } else {
            //System.out.println("Erro: Orçamento ou hotel não atribuído corretamente.");
        }
    }

    

    public void rejeitarOrcamento() {
        this.orcamentoAceito = false;
        //System.out.println(nome + " rejeitou o orçamento.");
    }

    // Getters e Setters
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOrigem() {
        return origem;
    }

    public void setorigem(String origem) {
        this.origem = origem;
    }

    public int getPreferenciaEstrelasHotel() {
        return preferenciaEstrelasHotel;
    }

    public void setPreferenciaEstrelasHotel(int preferenciaEstrelasHotel) {
        this.preferenciaEstrelasHotel = preferenciaEstrelasHotel;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public Orcamento getOrcamento() {
        return orcamento;
    }

    public void setOrcamento(Orcamento orcamento) {
        this.orcamento = orcamento;
    }

    public boolean isOrcamentoAceito() {
        return orcamentoAceito;
    }

    public void setOrcamentoAceito(boolean orcamentoAceito) {
        this.orcamentoAceito = orcamentoAceito;
    }

    public long getTempoAvaliacao() {
        return tempoAvaliacao;
    }

    public void setTempoAvaliacao(int tempoAvaliacao) {
        this.tempoAvaliacao = tempoAvaliacao;
    }

    public double getSaldo(){
        return saldo;
    }

    public void setSaldo(int saldo){
        this.saldo = saldo;
    }

    public int getQtdDiarias(){
        return qtdDiarias;
    }

    public void setQtdDiarias(int qtdDiarias){
        this.qtdDiarias = qtdDiarias;
    }
    
}


class Hotel {
    private String nome;
    private String localizacao;
    private int estrelas;
    private double diaria;
    private int quartosDisponiveis;

    public Hotel(){

    }

    public Hotel(String nome, String localizacao ,int estrelas , double diaria, int quartosDisponiveis){
        this.nome = nome;
        this.localizacao = localizacao;
        this.estrelas = estrelas;
        this.diaria = diaria;
        this.quartosDisponiveis = quartosDisponiveis;
    }

    public void reservarQuarto() {
        if (quartosDisponiveis > 0) {
            quartosDisponiveis--;
            //System.out.println("Quarto reservado em " + nome + ". Quartos disponíveis restantes: " + quartosDisponiveis);
        } else {
            //System.out.println("Não foi possível reservar quarto em " + nome + ". Não há quartos disponíveis.");
        }
    }
    
    public String getNome(){
        return nome;
    }

    public String getLocalizacao(){
        return localizacao;
    }
    public void setLocalizacao(String localizacao){
        this.localizacao = localizacao;
    }

    public int getEstrelas(){
        return estrelas;
    }
    public void setEstrelas(int estrelas){
        this.estrelas = estrelas;
    } 

    public double getDiaria(){
        return diaria;
    }
    public void setDiaria(int diaria){
        this.diaria = diaria;
    }

    public int getQuartosDisponiveis(){
        return quartosDisponiveis;
    }

}


class Voo{
    private String origem;
    private String destino;
    private double preco;
    private String dia;
    private String hora;
    private int assentosDisponiveis;

    public Voo(String origem, String destino, String dia, String hora, int assentosDisponiveis, double preco){
        this.origem = origem;
        this.destino = destino;
        this.preco = preco;
        this.dia = dia;
        this.hora = hora;
        this.assentosDisponiveis = assentosDisponiveis;
    }

    public String getOrigem() {
        return origem;
    }

    public void setOrigem(String origem) {
        this.origem = origem;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public void reservarAssento() {
        if (assentosDisponiveis > 0) {
            assentosDisponiveis--;
            //System.out.println("Assento reservado em voo de " + origem + " para " + destino + ". Assentos disponíveis restantes: " + assentosDisponiveis);
        } else {
            //System.out.println("Não foi possível reservar assento em voo");
        }
    }
}


class Agencia {
    private List<Hotel> hoteis;
    private List<Voo> voos;

    public Agencia() {
        this.hoteis = new ArrayList<>();
        this.voos = new ArrayList<>();
    }

    public void adicionarHotel(Hotel hotel) {
        this.hoteis.add(hotel);
    }

    public void adicionarVoo(Voo voo) {
        this.voos.add(voo);
    }

    public Orcamento gerarOrcamento(Usuario usuario) {
    
        List<Voo> voosEncontrados = new ArrayList<>();
        Voo vooMaisBarato = buscarVooMaisBarato(usuario.getOrigem(), usuario.getDestino());
        
        if (vooMaisBarato != null) {
            voosEncontrados.add(vooMaisBarato);
        } 
        else {
            Voo primeiraConexao = buscarVooMaisBarato(usuario.getOrigem(), "CNF"); 
            Voo segundaConexao = buscarVooMaisBarato("CNF", usuario.getDestino());

            if (primeiraConexao != null && segundaConexao != null) {
                voosEncontrados.add(primeiraConexao);
                voosEncontrados.add(segundaConexao);
            }
        }
    
        Hotel hotelMaisBarato = buscarHotelMaisBarato(usuario.getDestino(), usuario.getPreferenciaEstrelasHotel());
    
        if (!voosEncontrados.isEmpty() && hotelMaisBarato != null) {
            Orcamento orcamento = new Orcamento(voosEncontrados, hotelMaisBarato, usuario);

            //System.out.println("Orçamento gerado para o usuário: " + usuario.getNome());

            //System.out.println("Voo encontrado:");
            for (Voo voo : voosEncontrados) {
               // System.out.println(" - " + voo.getOrigem() + " -> " + voo.getDestino() + " por R$" + voo.getPreco());
            }

            //System.out.println("Hotel encontrado:");
            //System.out.println(" - " + hotelMaisBarato.getNome() + " em " + hotelMaisBarato.getLocalizacao() + " com " + hotelMaisBarato.getEstrelas() + " estrelas por R$" + hotelMaisBarato.getDiaria());
            //System.out.println("Custo total: R$" + orcamento.getCustoTotal());
    
            return orcamento;
        } else {
            //System.out.println("Não foi possível gerar o orçamento para o usuário: " + usuario.getNome());
            if (voosEncontrados.isEmpty()) {
              //  System.out.println("Motivo: Nenhum voo encontrado.");
            }
            if (hotelMaisBarato == null) {
                //System.out.println("Motivo: Nenhum hotel encontrado ou disponível.");
            }
            return null;
        }
    }
    


    private Voo buscarVooMaisBarato(String origem, String destino) {
        Voo vooMaisBarato = null;
        double menorPreco = Double.MAX_VALUE;

        for (Voo voo : voos) {
            if (voo.getOrigem().equals(origem) && voo.getDestino().equals(destino)) {
                if (voo.getPreco() < menorPreco) {
                    menorPreco = voo.getPreco();
                    vooMaisBarato = voo;
                }
            }
        }
        return vooMaisBarato;
    }


    private Hotel buscarHotelMaisBarato(String localizacao, int estrelasMinimas) {
        Hotel hotelMaisBarato = null;
        double menorDiaria = Double.MAX_VALUE;
    
        for (Hotel hotel : hoteis) {
            if (hotel.getLocalizacao().equals(localizacao) && hotel.getEstrelas() >= estrelasMinimas && hotel.getQuartosDisponiveis() > 0) {
                if (hotel.getDiaria() < menorDiaria) {
                    menorDiaria = hotel.getDiaria();
                    hotelMaisBarato = hotel;
                }
            }
        }

        return hotelMaisBarato;
    }
    


    public void processarUsuariosSequencialmente(List<Usuario> usuarios) {
        for (Usuario usuario : usuarios) {
            //System.out.println("Processando usuário sequencialmente: " + usuario.getNome());
            Orcamento orcamento = gerarOrcamento(usuario);
            if (orcamento != null && usuario.getSaldo() >= orcamento.getCustoTotal()) {
                usuario.setOrcamento(orcamento);
                usuario.aceitarOrcamento();

            } else {
                usuario.rejeitarOrcamento();
                //System.out.println("Orçamento rejeitado por: " + usuario.getNome());
            }
        }
    }

    public void processarUsuariosParalelamente(List<Usuario> usuarios) {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (Usuario usuario : usuarios) {
            executor.submit(() -> {
                //System.out.println("Processando usuário paralelamente: " + usuario.getNome());
                Orcamento orcamento = gerarOrcamento(usuario);
                usuario.setOrcamento(orcamento);
                synchronized (this) {
                    if (orcamento != null && usuario.getSaldo() >= orcamento.getCustoTotal()) {
                        usuario.aceitarOrcamento();
                        //System.out.println("Orçamento aceito por: " + usuario.getNome());
                    } else {
                        usuario.rejeitarOrcamento();
                        //System.out.println("Orçamento rejeitado por: " + usuario.getNome());
                    }
                }
            });
        }
        executor.shutdown();
    }
}

class Orcamento {
    private List<Voo> voos;
    private Hotel hotel;
    private Usuario usuario;

    public Orcamento(){

    }

    public Orcamento(List<Voo> voos, Hotel hotel, Usuario usuario) {
        this.voos = voos;
        this.hotel = hotel;
        this.usuario = usuario;
    }

    public double getCustoTotal() {
        double custoTotal = 0;
        for (Voo voo : voos) {
            custoTotal += voo.getPreco();
        }
        custoTotal += hotel.getDiaria() * usuario.getQtdDiarias();
        return custoTotal;
    }
    
    //getters and setters
    public List<Voo> getVoos() {
        return voos;
    }

    public Hotel getHotel() {
        return hotel;
    }

}