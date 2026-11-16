package com.rotacultural.rcservice.config;

import com.rotacultural.rcservice.entities.Categories;
import com.rotacultural.rcservice.entities.Feedbacks;
import com.rotacultural.rcservice.entities.Places;
import com.rotacultural.rcservice.entities.Users;
import com.rotacultural.rcservice.entities.enums.Price;
import com.rotacultural.rcservice.entities.enums.Role;
import com.rotacultural.rcservice.entities.enums.Status;
import com.rotacultural.rcservice.repositories.CategoriesRepository;
import com.rotacultural.rcservice.repositories.FeedbacksRepository;
import com.rotacultural.rcservice.repositories.PlacesRepository;
import com.rotacultural.rcservice.repositories.UsersRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsersRepository usersRepository;
    private final CategoriesRepository categoriesRepository;
    private final PlacesRepository placesRepository;
    private final FeedbacksRepository feedbacksRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            UsersRepository usersRepository,
            CategoriesRepository categoriesRepository,
            PlacesRepository placesRepository,
            FeedbacksRepository feedbacksRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usersRepository = usersRepository;
        this.categoriesRepository = categoriesRepository;
        this.placesRepository = placesRepository;
        this.feedbacksRepository = feedbacksRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Users admin = ensureUser(
                "Administrador Rota Cultural",
                "admin@rotacultural.com",
                "Senha@123",
                "01310-100",
                Role.ADMIN,
                "Equipe de curadoria e moderação cultural do Rota Cultural."
        );
        Users nativo = ensureUser(
                "Mariana Nativa",
                "nativo@rotacultural.com",
                "Senha@123",
                "23970-000",
                Role.NATIVO,
                "Moradora local apaixonada por história, museus e roteiros autênticos."
        );
        Users usuario = ensureUser(
                "Rafael Viajante",
                "usuario@rotacultural.com",
                "Senha@123",
                "04094-050",
                Role.USUARIO,
                "Viajante explorando experiências culturais pelo Brasil."
        );

        Map<String, Categories> categories = new HashMap<>();
        categories.put("historia", ensureCategory("História & Patrimônio"));
        categories.put("natureza", ensureCategory("Natureza & Ecoturismo"));
        categories.put("gastronomia", ensureCategory("Gastronomia & Sabores"));
        categories.put("arte", ensureCategory("Arte & Museus"));
        categories.put("praias", ensureCategory("Praias & Litoral"));
        categories.put("aventura", ensureCategory("Aventura & Trilhas"));
        categories.put("urbano", ensureCategory("Vida Urbana & Cultura"));

        Places masp = ensurePlace(
                "Museu de Arte de São Paulo (MASP)",
                "Principal museu de arte ocidental da América Latina, referência arquitetônica na Avenida Paulista com acervo permanente e exposições temporárias.",
                categories.get("arte"),
                "Av. Paulista, 1578 - Bela Vista",
                "São Paulo",
                "SP",
                "Brasil",
                new BigDecimal("-23.561414"),
                new BigDecimal("-46.655881"),
                "Terça a Domingo: 10h às 18h (Consulte site oficial para feriados)",
                Price.MODERADO,
                null,
                "https://masp.org.br",
                "https://masp.org.br/ingressos",
                "contato@masp.org.br",
                admin,
                Status.APROVADO,
                null
        );

        Places ibirapuera = ensurePlace(
                "Parque Ibirapuera",
                "Maior parque urbano de São Paulo, reunindo áreas verdes, museus, auditório, pavilhões culturais e espaços ao ar livre para caminhadas.",
                categories.get("natureza"),
                "Av. Pedro Álvares Cabral, s/n - Vila Mariana",
                "São Paulo",
                "SP",
                "Brasil",
                new BigDecimal("-23.587416"),
                new BigDecimal("-46.657634"),
                "Todos os dias: 05h às 00h",
                Price.GRATUITO,
                BigDecimal.ZERO,
                "https://parqueibirapuera.org",
                null,
                null,
                nativo,
                Status.APROVADO,
                null
        );

        Places mercadao = ensurePlace(
                "Mercado Municipal de São Paulo",
                "Espaço histórico inaugurado em 1933, famoso pelos vitrais, bancas de frutas e gastronomia tradicional no centro paulistano.",
                categories.get("gastronomia"),
                "Rua da Cantareira, 306 - Centro Histórico",
                "São Paulo",
                "SP",
                "Brasil",
                new BigDecimal("-23.541677"),
                new BigDecimal("-46.629394"),
                "Segunda a Sábado: 06h às 18h; Domingo: 06h às 16h",
                Price.BARATO,
                null,
                null,
                null,
                null,
                nativo,
                Status.APROVADO,
                null
        );

        ensurePlace(
                "Pinacoteca de São Paulo",
                "Museu de artes visuais mais antigo da cidade de São Paulo, instalado no histórico edifício do Parque da Luz.",
                categories.get("arte"),
                "Praça da Luz, 2 - Luz",
                "São Paulo",
                "SP",
                "Brasil",
                new BigDecimal("-23.534292"),
                new BigDecimal("-46.633796"),
                "Quarta a Segunda: 10h às 18h",
                Price.BARATO,
                null,
                "https://pinacoteca.org.br",
                null,
                null,
                admin,
                Status.APROVADO,
                null
        );

        Places cristo = ensurePlace(
                "Cristo Redentor - Parque Nacional da Tijuca",
                "Monumento histórico no topo do Morro do Corcovado com vista panorâmica da Baía de Guanabara e da Floresta da Tijuca.",
                categories.get("historia"),
                "Parque Nacional da Tijuca - Alto da Boa Vista",
                "Rio de Janeiro",
                "RJ",
                "Brasil",
                new BigDecimal("-22.951916"),
                new BigDecimal("-43.210487"),
                "Todos os dias: 08h às 19h (Consulte bilheteria oficial)",
                Price.CARO,
                null,
                "https://www.tremdocorcovado.rio",
                "https://www.tremdocorcovado.rio",
                null,
                admin,
                Status.APROVADO,
                null
        );

        Places selaron = ensurePlace(
                "Escadaria Selarón",
                "Obra urbana icônica revestida por mais de dois mil azulejos coloridos ligando os bairros da Lapa e Santa Teresa.",
                categories.get("urbano"),
                "Rua Manuel Carneiro - Santa Teresa",
                "Rio de Janeiro",
                "RJ",
                "Brasil",
                new BigDecimal("-22.915294"),
                new BigDecimal("-43.179246"),
                "Aberto 24 horas (Acesso livre ao logradouro)",
                Price.GRATUITO,
                BigDecimal.ZERO,
                null,
                null,
                null,
                nativo,
                Status.APROVADO,
                null
        );

        ensurePlace(
                "Museu do Amanhã",
                "Museu de ciências aplicado no Píer Mauá dedicado às transformações contemporâneas e sustentabilidade urbana.",
                categories.get("arte"),
                "Praça Mauá, 1 - Centro",
                "Rio de Janeiro",
                "RJ",
                "Brasil",
                new BigDecimal("-22.893938"),
                new BigDecimal("-43.179470"),
                "Terça a Domingo: 10h às 18h",
                Price.BARATO,
                null,
                "https://museudoamanha.org.br",
                null,
                null,
                admin,
                Status.APROVADO,
                null
        );

        Places paraty = ensurePlace(
                "Centro Histórico de Paraty",
                "Conjunto arquitetônico colonial preservado com ruas de pedra pé-de-moleque, igrejas históricas e ateliês culturais.",
                categories.get("historia"),
                "Praça da Matriz, Centro Histórico",
                "Paraty",
                "RJ",
                "Brasil",
                new BigDecimal("-23.219498"),
                new BigDecimal("-44.713163"),
                "Acesso livre durante todo o dia (Consulte horário individual de igrejas e museus)",
                Price.GRATUITO,
                BigDecimal.ZERO,
                null,
                null,
                null,
                nativo,
                Status.APROVADO,
                null
        );

        ensurePlace(
                "Praia do Sono",
                "Praia cercada por Mata Atlântica na região de Paraty, acessível por trilha costeira ou embarcação local a partir do Condomínio Laranjeiras.",
                categories.get("praias"),
                "Vila Oratório - Laranjeiras",
                "Paraty",
                "RJ",
                "Brasil",
                new BigDecimal("-23.332415"),
                new BigDecimal("-44.637760"),
                "Acesso diurno recomendado entre 07h e 17h",
                Price.GRATUITO,
                BigDecimal.ZERO,
                null,
                null,
                null,
                nativo,
                Status.APROVADO,
                null
        );

        Places ouroPreto = ensurePlace(
                "Praça Tiradentes e Museu da Inconfidência",
                "Coração histórico de Ouro Preto cercado por casarões setecentistas, monumentos e o Museu da Inconfidência.",
                categories.get("historia"),
                "Praça Tiradentes, 139 - Centro",
                "Ouro Preto",
                "MG",
                "Brasil",
                new BigDecimal("-20.385574"),
                new BigDecimal("-43.503578"),
                "Terça a Domingo: 10h às 18h",
                Price.BARATO,
                null,
                null,
                null,
                null,
                admin,
                Status.APROVADO,
                null
        );

        ensurePlace(
                "Igreja de São Francisco de Assis",
                "Obra-prima do barroco mineiro projetada por Aleijadinho com pintura de teto de Mestre Ataíde.",
                categories.get("historia"),
                "Largo de Coimbra, s/n - Centro",
                "Ouro Preto",
                "MG",
                "Brasil",
                new BigDecimal("-20.386614"),
                new BigDecimal("-43.502843"),
                "Terça a Domingo: 08h30 às 17h",
                Price.BARATO,
                null,
                null,
                null,
                null,
                nativo,
                Status.APROVADO,
                null
        );

        ensurePlace(
                "Pelourinho - Centro Histórico de Salvador",
                "Sítio histórico patrimônio mundial com ladeiras de pedra, largos culturais, música percussiva e culinária baiana.",
                categories.get("historia"),
                "Largo do Pelourinho - Centro Histórico",
                "Salvador",
                "BA",
                "Brasil",
                new BigDecimal("-12.971599"),
                new BigDecimal("-38.508256"),
                "Acesso livre ao logradouro; estabelecimentos em horário comercial",
                Price.GRATUITO,
                BigDecimal.ZERO,
                null,
                null,
                null,
                admin,
                Status.APROVADO,
                null
        );

        // Locais pendentes de moderação (EM_AVALIACAO) criados pelo NATIVO
        ensurePlace(
                "Mirante do Morro do Elefante",
                "Ponto panorâmico com vista para a Serra da Mantiqueira, indicado para contemplação no fim da tarde.",
                categories.get("aventura"),
                "Estrada do Morro do Elefante, s/n",
                "Campos do Jordão",
                "SP",
                "Brasil",
                new BigDecimal("-22.724344"),
                new BigDecimal("-45.573432"),
                "Todos os dias: 09h às 18h",
                Price.GRATUITO,
                BigDecimal.ZERO,
                null,
                null,
                null,
                nativo,
                Status.EM_AVALIACAO,
                null
        );

        ensurePlace(
                "Feira de Artesanato da Praça da República",
                "Feira tradicional aos domingos reunindo artistas plásticos, gastronomia popular e artesanato regional.",
                categories.get("urbano"),
                "Praça da República, s/n - República",
                "São Paulo",
                "SP",
                "Brasil",
                new BigDecimal("-23.543252"),
                new BigDecimal("-46.642577"),
                "Domingos: 09h às 17h",
                Price.GRATUITO,
                BigDecimal.ZERO,
                null,
                null,
                null,
                nativo,
                Status.EM_AVALIACAO,
                null
        );

        // Exemplo de local rejeitado com motivo para o fluxo de revisão do NATIVO
        ensurePlace(
                "Ponto Cultural Exemplo Pendente de Revisão",
                "Cadastro demonstrativo utilizado para validar o fluxo de revisão de locais rejeitados pelo administrador.",
                categories.get("urbano"),
                "Rua das Flores, 100",
                "São Paulo",
                "SP",
                "Brasil",
                new BigDecimal("-23.550520"),
                new BigDecimal("-46.633308"),
                "A confirmar",
                Price.BARATO,
                null,
                null,
                null,
                null,
                nativo,
                Status.REJEITADO,
                "Informe horário de funcionamento exato e endereço completo para nova avaliação."
        );

        ensureFeedback(usuario, masp, 5.0, "Acervo excelente e localização muito acessível pela Avenida Paulista.");
        ensureFeedback(usuario, ibirapuera, 5.0, "Ótimo espaço público para passeios culturais e contato com a natureza.");
        ensureFeedback(nativo, paraty, 5.0, "Caminhada histórica imperdível pelo calçamento colonial.");
        ensureFeedback(usuario, ouroPreto, 4.5, "Conjunto arquitetônico preservado e museu muito bem organizado.");
        ensureFeedback(nativo, cristo, 4.5, "Vista panorâmica impressionante da cidade do Rio de Janeiro.");
        ensureFeedback(usuario, selaron, 4.0, "Ambiente vibrante com muita arte urbana, recomendo visitar cedo.");
        ensureFeedback(nativo, mercadao, 4.5, "Vitrais históricos lindos e ótima variedade gastronômica.");
    }

    private Users ensureUser(
            String name,
            String email,
            String rawPassword,
            String cep,
            Role role,
            String bio
    ) {
        return usersRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> {
                    Users user = new Users();
                    user.setName(name);
                    user.setEmail(email);
                    user.setPassword(passwordEncoder.encode(rawPassword));
                    user.setCep(cep);
                    user.setRole(role);
                    user.setNativeRequested(false);
                    user.setBio(bio);
                    return usersRepository.save(user);
                });
    }

    private Categories ensureCategory(String name) {
        return categoriesRepository.findAll().stream()
                .filter(c -> c.getName() != null && c.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> {
                    Categories cat = new Categories();
                    cat.setName(name);
                    return categoriesRepository.save(cat);
                });
    }

    private Places ensurePlace(
            String name,
            String description,
            Categories category,
            String address,
            String city,
            String state,
            String country,
            BigDecimal latitude,
            BigDecimal longitude,
            String businessHours,
            Price price,
            BigDecimal estimatedCost,
            String officialWebsite,
            String ticketLink,
            String contact,
            Users creator,
            Status status,
            String rejectionReason
    ) {
        if (placesRepository.existsByNameIgnoreCaseAndCityIgnoreCase(name, city)) {
            return placesRepository.findAll().stream()
                    .filter(p -> p.getName().equalsIgnoreCase(name) && p.getCity().equalsIgnoreCase(city))
                    .findFirst()
                    .orElseThrow();
        }
        Places place = new Places();
        place.setName(name);
        place.setDescription(description);
        place.setCategory(category);
        place.setAddress(address);
        place.setCity(city);
        place.setState(state);
        place.setCountry(country);
        place.setLatitude(latitude);
        place.setLongitude(longitude);
        place.setBusinessHours(businessHours);
        place.setPrice(price);
        place.setEstimatedCost(estimatedCost);
        place.setOfficialWebsite(officialWebsite);
        place.setTicketLink(ticketLink);
        place.setContact(contact);
        place.setCreateBy(creator);
        place.setStatus(status);
        place.setRejectionReason(rejectionReason);
        return placesRepository.save(place);
    }

    private void ensureFeedback(Users user, Places place, Double assessment, String commentary) {
        if (user == null || place == null) {
            return;
        }
        if (!feedbacksRepository.existsByUser_IdUserAndPlaces_IdLoc(user.getIdUser(), place.getIdLoc())) {
            Feedbacks feedback = new Feedbacks();
            feedback.setUser(user);
            feedback.setPlaces(place);
            feedback.setAssessment(assessment);
            feedback.setCommentary(commentary);
            feedbacksRepository.save(feedback);
        }
    }
}
