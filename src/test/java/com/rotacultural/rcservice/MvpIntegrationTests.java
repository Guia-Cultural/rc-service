package com.rotacultural.rcservice;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rotacultural.rcservice.config.JwtConfig;
import com.rotacultural.rcservice.entities.Categories;
import com.rotacultural.rcservice.entities.Users;
import com.rotacultural.rcservice.entities.enums.Role;
import com.rotacultural.rcservice.repositories.CategoriesRepository;
import com.rotacultural.rcservice.repositories.UsersRepository;
import com.rotacultural.rcservice.services.storage.AzureBlobStorageService;
import com.rotacultural.rcservice.services.storage.ImageFileValidator;
import com.rotacultural.rcservice.services.storage.StoredResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MvpIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private CategoriesRepository categoriesRepository;

    @Autowired
    private JwtEncoder jwtEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String adminToken;
    private String nativoToken;
    private String usuarioToken;
    private Users usuarioSeed;
    private Users nativoSeed;
    private Long defaultCategoryId;

    @BeforeEach
    void setup() throws Exception {
        adminToken = loginAndGetToken("admin@rotacultural.com", "Senha@123");
        nativoToken = loginAndGetToken("nativo@rotacultural.com", "Senha@123");
        usuarioToken = loginAndGetToken("usuario@rotacultural.com", "Senha@123");

        usuarioSeed = usersRepository.findByEmailIgnoreCase("usuario@rotacultural.com").orElseThrow();
        nativoSeed = usersRepository.findByEmailIgnoreCase("nativo@rotacultural.com").orElseThrow();

        Categories cat = categoriesRepository.findAll().stream().findFirst().orElseThrow();
        defaultCategoryId = cat.getIdCat();
    }

    @Test
    void publicRegistrationShouldEnforceRoleSafetyAndRejectDuplicateEmail() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "novo." + suffix + "@rotacultural.com";

        // Tentativa de autocadastro como ADMIN deve falhar com 400
        String adminRegisterJson = """
                {
                  "name": "Tentativa Admin",
                  "email": "hack.%s@rotacultural.com",
                  "password": "Password123",
                  "role": "ADMIN"
                }
                """.formatted(suffix);
        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adminRegisterJson))
                .andExpect(status().isBadRequest());

        // Cadastro solicitando NATIVO deve iniciar como USUARIO com nativeRequested=true
        String nativeRequestJson = """
                {
                  "name": "Novo Morador",
                  "email": "%s",
                  "password": "Password123",
                  "cep": "01310-100",
                  "role": "NATIVO",
                  "requestNative": true
                }
                """.formatted(email);
        MvcResult createResult = mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(nativeRequestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("USUARIO"))
                .andExpect(jsonPath("$.nativeRequested").value(true))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn();

        Long createdId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        // E-mail duplicado deve retornar 409 Conflict
        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(nativeRequestJson))
                .andExpect(status().isConflict());

        // ADMIN aprova a solicitação para NATIVO
        mockMvc.perform(patch("/user/" + createdId + "/role")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"NATIVO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("NATIVO"))
                .andExpect(jsonPath("$.nativeRequested").value(false));
    }

    @Test
    void authenticationShouldValidateCredentialsValidTokenMissingTokenAndExpiredToken() throws Exception {
        // Sem token -> 401
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());

        // Senha errada -> 401
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"usuario@rotacultural.com\",\"password\":\"errada123\"}"))
                .andExpect(status().isUnauthorized());

        // Token válido -> 200
        mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer " + usuarioToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("usuario@rotacultural.com"))
                .andExpect(jsonPath("$.role").value("USUARIO"));

        // Token expirado -> 401
        Instant past = Instant.now().minusSeconds(3600);
        JwtClaimsSet expiredClaims = JwtClaimsSet.builder()
                .issuer("rc-service")
                .issuedAt(past.minusSeconds(60))
                .expiresAt(past)
                .subject(String.valueOf(usuarioSeed.getIdUser()))
                .claim("role", List.of(Role.USUARIO.name()))
                .build();
        String expiredToken = jwtEncoder.encode(
                JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), expiredClaims)
        ).getTokenValue();

        mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void roleHierarchyAndOwnershipShouldBlockUnauthorizedAccess() throws Exception {
        // USUARIO não pode cadastrar local (403)
        String placeJson = """
                {
                  "name": "Local Proibido",
                  "description": "Tentativa de usuário comum",
                  "catId": %d,
                  "address": "Rua Teste, 10",
                  "city": "São Paulo",
                  "state": "SP",
                  "country": "Brasil",
                  "businessHours": "09h às 17h",
                  "price": "GRATUITO"
                }
                """.formatted(defaultCategoryId);

        mockMvc.perform(post("/places")
                        .header("Authorization", "Bearer " + usuarioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(placeJson))
                .andExpect(status().isForbidden());

        // USUARIO não pode listar todos os usuários (403) nem editar outro usuário (403)
        mockMvc.perform(get("/user")
                        .header("Authorization", "Bearer " + usuarioToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/user/" + nativoSeed.getIdUser())
                        .header("Authorization", "Bearer " + usuarioToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/user/" + nativoSeed.getIdUser())
                        .header("Authorization", "Bearer " + usuarioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Invasor\",\"email\":\"nativo@rotacultural.com\"}"))
                .andExpect(status().isForbidden());

        // USUARIO cria uma viagem própria (userId do token prevalece)
        MvcResult tripRes = mockMvc.perform(post("/trip")
                        .header("Authorization", "Bearer " + usuarioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "destination": "São Paulo",
                                  "startDate": "2026-12-01",
                                  "endDate": "2026-12-03",
                                  "budget": 300.00
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        Long usuarioTripId = objectMapper.readTree(tripRes.getResponse().getContentAsString()).get("id").asLong();

        // NATIVO (outro usuário, não ADMIN) não pode acessar nem excluir a viagem do USUARIO
        mockMvc.perform(get("/trip/" + usuarioTripId)
                        .header("Authorization", "Bearer " + nativoToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/trip/" + usuarioTripId)
                        .header("Authorization", "Bearer " + nativoToken))
                .andExpect(status().isForbidden());

        // Tentativa de manipular favorito de outro usuário -> 403
        mockMvc.perform(post("/favorites")
                        .header("Authorization", "Bearer " + nativoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":" + usuarioSeed.getIdUser() + ",\"locId\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void placeLifecycleModerationImageUploadAndPublicVisibilityShouldWork() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String placeName = "Centro Cultural Teste " + suffix;

        // Mesmo enviando userId de outro e status APROVADO, backend força creator=NATIVO e status=EM_AVALIACAO
        String createPlaceJson = """
                {
                  "name": "%s",
                  "description": "Espaço cultural de teste completo",
                  "catId": %d,
                  "address": "Rua da Cultura, 250",
                  "city": "Campinas",
                  "state": "SP",
                  "country": "Brasil",
                  "latitude": -22.90556,
                  "longitude": -47.06083,
                  "businessHours": "Terça a Domingo: 10h às 18h",
                  "price": "BARATO",
                  "estimatedCost": 25.00,
                  "userId": %d,
                  "status": "APROVADO"
                }
                """.formatted(placeName, defaultCategoryId, usuarioSeed.getIdUser());

        MvcResult created = mockMvc.perform(post("/places")
                        .header("Authorization", "Bearer " + nativoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPlaceJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("EM_AVALIACAO"))
                .andExpect(jsonPath("$.userId").value(nativoSeed.getIdUser()))
                .andReturn();

        Long placeId = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        // Enquanto EM_AVALIACAO, não aparece na consulta pública
        mockMvc.perform(get("/places"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", not(hasItem(placeName))));

        mockMvc.perform(get("/places/" + placeId))
                .andExpect(status().isNotFound());

        // NATIVO não pode autoaprovar o local
        mockMvc.perform(patch("/places/" + placeId + "/status")
                        .header("Authorization", "Bearer " + nativoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"APROVADO\"}"))
                .andExpect(status().isForbidden());

        // Upload de imagem PNG válida pelo NATIVO criador
        byte[] validPngHeader = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D};
        MockMultipartFile pngFile = new MockMultipartFile(
                "files", "fachada.png", "image/png", validPngHeader
        );

        MvcResult uploadResult = mockMvc.perform(multipart("/places/" + placeId + "/images")
                        .file(pngFile)
                        .param("cover", "true")
                        .header("Authorization", "Bearer " + nativoToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].cover").value(true))
                .andReturn();
        JsonNode imageNode = objectMapper.readTree(uploadResult.getResponse().getContentAsString()).get(0);
        Long imageId = imageNode.get("id").asLong();
        String signedImageUrl = imageNode.get("url").asText();
        String canonicalImageUrl = ImageFileValidator.stripQuery(signedImageUrl);

        // Enquanto EM_AVALIACAO: acesso anônimo sem assinatura -> 403; com assinatura ou ADMIN -> 200
        mockMvc.perform(get(canonicalImageUrl))
                .andExpect(status().isForbidden());
        mockMvc.perform(get(canonicalImageUrl + "?exp=1&sig=invalida"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get(signedImageUrl))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.IMAGE_PNG));
        mockMvc.perform(get(canonicalImageUrl)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Upload com conteúdo inválido disfarçado de PNG deve ser bloqueado (400)
        MockMultipartFile invalidFile = new MockMultipartFile(
                "files", "fake.png", "image/png", "not-an-image".getBytes()
        );
        mockMvc.perform(multipart("/places/" + placeId + "/images")
                        .file(invalidFile)
                        .header("Authorization", "Bearer " + nativoToken))
                .andExpect(status().isBadRequest());

        // ADMIN aprova o local
        mockMvc.perform(patch("/places/" + placeId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"APROVADO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADO"));

        // Local aprovado expõe foto pública sem necessidade de assinatura e aparece no catálogo
        mockMvc.perform(get(canonicalImageUrl))
                .andExpect(status().isOk());
        mockMvc.perform(get("/places").param("city", "Campinas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItem(placeName)));

        // Rejeição de outro local mantém oculto da vitrine pública
        String rejectedName = "Local Rejeitado Teste " + suffix;
        MvcResult rejectedCreate = mockMvc.perform(post("/places")
                        .header("Authorization", "Bearer " + nativoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPlaceJson.replace(placeName, rejectedName)))
                .andExpect(status().isCreated())
                .andReturn();
        Long rejectedId = objectMapper.readTree(rejectedCreate.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(patch("/places/" + rejectedId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"REJEITADO\",\"rejectionReason\":\"Dados incompletos\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJEITADO"));

        mockMvc.perform(get("/places"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", not(hasItem(rejectedName))));

        // Exclusão da imagem pelo dono
        mockMvc.perform(delete("/places/" + placeId + "/images/" + imageId)
                        .header("Authorization", "Bearer " + nativoToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void tripsValidationFavoritesFeedbacksRecommendationsAndItineraryGenerationShouldWork() throws Exception {
        // Data final anterior à inicial -> 400
        mockMvc.perform(post("/trip")
                        .header("Authorization", "Bearer " + usuarioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "destination": "São Paulo",
                                  "startDate": "2026-12-10",
                                  "endDate": "2026-12-05",
                                  "budget": 500
                                }
                                """))
                .andExpect(status().isBadRequest());

        // Período maior que 30 dias -> 400
        mockMvc.perform(post("/trip")
                        .header("Authorization", "Bearer " + usuarioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "destination": "São Paulo",
                                  "startDate": "2026-12-01",
                                  "endDate": "2027-01-05",
                                  "budget": 500
                                }
                                """))
                .andExpect(status().isBadRequest());

        // Criação válida com preferências
        MvcResult tripResult = mockMvc.perform(post("/trip")
                        .header("Authorization", "Bearer " + usuarioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "destination": "São Paulo",
                                  "startDate": "2026-12-01",
                                  "endDate": "2026-12-03",
                                  "budget": 400.00,
                                  "categoryIds": [%d]
                                }
                                """.formatted(defaultCategoryId)))
                .andExpect(status().isCreated())
                .andReturn();
        Long tripId = objectMapper.readTree(tripResult.getResponse().getContentAsString()).get("id").asLong();

        // Recomendações reais para a viagem
        MvcResult recResult = mockMvc.perform(get("/recommendations/trip/" + tripId)
                        .header("Authorization", "Bearer " + usuarioToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.places").isArray())
                .andReturn();
        JsonNode recPlaces = objectMapper.readTree(recResult.getResponse().getContentAsString()).get("places");
        assertThat(recPlaces.size()).isGreaterThan(0);

        // Gerar e consultar roteiro salvo
        mockMvc.perform(post("/itineraries/generate/" + tripId)
                        .header("Authorization", "Bearer " + usuarioToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].tripId").value(tripId));

        mockMvc.perform(get("/itineraries/trip/" + tripId)
                        .header("Authorization", "Bearer " + usuarioToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].placeCity").value("São Paulo"));

        // Favoritar e desfavoritar local aprovado
        Long approvedPlaceId = recPlaces.get(0).get("place").get("id").asLong();
        mockMvc.perform(post("/favorites")
                        .header("Authorization", "Bearer " + usuarioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"locId\":" + approvedPlaceId + "}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/favorites/me")
                        .header("Authorization", "Bearer " + usuarioToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].locId", hasItem(approvedPlaceId.intValue())));

        mockMvc.perform(delete("/favorites/place/" + approvedPlaceId)
                        .header("Authorization", "Bearer " + usuarioToken))
                .andExpect(status().isNoContent());

        // Avaliação: uma por usuário/local, com edição permitida
        MvcResult placesRes = mockMvc.perform(get("/places").param("city", "Salvador")).andReturn();
        Long salvadorPlaceId = objectMapper.readTree(placesRes.getResponse().getContentAsString()).get(0).get("id").asLong();

        MvcResult feedRes = mockMvc.perform(post("/feedbacks")
                        .header("Authorization", "Bearer " + usuarioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "locId": %d,
                                  "assessment": 4.0,
                                  "commentary": "Muito bom!"
                                }
                                """.formatted(salvadorPlaceId)))
                .andExpect(status().isCreated())
                .andReturn();
        Long feedId = objectMapper.readTree(feedRes.getResponse().getContentAsString()).get("id").asLong();

        // Segunda avaliação do mesmo usuário no mesmo local -> 409
        mockMvc.perform(post("/feedbacks")
                        .header("Authorization", "Bearer " + usuarioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "locId": %d,
                                  "assessment": 5.0,
                                  "commentary": "Duplicada"
                                }
                                """.formatted(salvadorPlaceId)))
                .andExpect(status().isConflict());

        // Edição da própria avaliação -> 200
        mockMvc.perform(put("/feedbacks/" + feedId)
                        .header("Authorization", "Bearer " + usuarioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "locId": %d,
                                  "assessment": 5.0,
                                  "commentary": "Experiência incrível!"
                                }
                                """.formatted(salvadorPlaceId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assessment").value(5.0));
    }

    @Test
    void healthProdSecretGuardAndAzureBlobServiceShouldWork() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.storageProvider").value("local"));

        JwtConfig jwtConfig = new JwtConfig();
        MockEnvironment prodEnv = new MockEnvironment().withProperty("dummy", "1");
        prodEnv.setActiveProfiles("prod");
        assertThatThrownBy(() -> jwtConfig.jwtSecretKey("", prodEnv))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> jwtConfig.jwtSecretKey(JwtConfig.DEV_DEFAULT_SECRET_BASE64, prodEnv))
                .isInstanceOf(IllegalStateException.class);

        // Valida AzureBlobStorageService armazenando referência canônica (sem URL SAS no banco)
        BlobContainerClient containerClient = Mockito.mock(BlobContainerClient.class);
        BlobClient blobClient = Mockito.mock(BlobClient.class);
        Mockito.when(containerClient.exists()).thenReturn(true);
        Mockito.when(containerClient.getBlobClient(Mockito.anyString())).thenReturn(blobClient);
        Mockito.when(blobClient.exists()).thenReturn(true);
        byte[] validPng = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D};
        Mockito.when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(validPng));

        AzureBlobStorageService azureStorage = new AzureBlobStorageService(containerClient);
        MockMultipartFile file = new MockMultipartFile("files", "azure.png", "image/png", validPng);
        String storedUrl = azureStorage.store(file, "places/99");
        assertThat(storedUrl).startsWith("/uploads/places/99/").endsWith(".png");
        assertThat(azureStorage.providerName()).isEqualTo("azure");

        StoredResource loaded = azureStorage.load(storedUrl);
        assertThat(loaded.content()).isEqualTo(validPng);
        assertThat(loaded.contentType()).isEqualTo("image/png");

        azureStorage.delete(storedUrl);
        Mockito.verify(blobClient).deleteIfExists();
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        MvcResult res = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString()).get("token").asText();
    }
}
