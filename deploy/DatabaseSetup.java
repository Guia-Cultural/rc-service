import java.sql.DriverManager;
import org.flywaydb.core.Flyway;

// Executar com o classpath Maven de runtime. Credenciais apenas no ambiente do processo.
// Sem baseline automático, limpeza, DROP ou alteração de senha de usuário existente.
class DatabaseSetup {
    public static void main(String[] args) throws Exception {
        String url = System.getenv("DB_URL");
        String user = System.getenv("DB_USERNAME");
        String password = System.getenv("DB_PASSWORD");
        String mode = args.length == 0 ? "audit" : args[0];
        if (url == null || !url.contains("sslMode=VERIFY_IDENTITY")) {
            throw new IllegalArgumentException("DB_URL deve validar certificado e hostname TLS");
        }
        try (var connection = DriverManager.getConnection(url, user, password);
             var statement = connection.createStatement()) {
            try (var rs = statement.executeQuery("SHOW SESSION STATUS LIKE 'Ssl_cipher'")) {
                if (!rs.next() || rs.getString(2).isBlank()) throw new IllegalStateException("TLS ausente");
                System.out.println("TLS verificado: " + rs.getString(2));
            }
            try (var rs = statement.executeQuery("SELECT table_name FROM information_schema.tables WHERE table_schema=DATABASE() ORDER BY table_name")) {
                while (rs.next()) System.out.println("Tabela: " + rs.getString(1));
            }
            if (mode.equals("migrate")) {
                var flyway = Flyway.configure().dataSource(url, user, password)
                        .locations("filesystem:src/main/resources/db/migration")
                        .cleanDisabled(true).baselineOnMigrate(false).load();
                System.out.println("Migrações aplicadas: " + flyway.migrate().migrationsExecuted);
                flyway.validate();
            } else if (mode.equals("provision-user")) {
                String appPassword = System.getenv("RC_APP_DB_PASSWORD");
                if (appPassword == null || appPassword.length() < 24) throw new IllegalArgumentException("Senha de aplicação ausente/fraca");
                try (var create = connection.prepareStatement("CREATE USER IF NOT EXISTS 'rc_app'@'%' IDENTIFIED BY ? REQUIRE SSL")) {
                    create.setString(1, appPassword);
                    create.execute();
                }
                statement.execute("GRANT SELECT, INSERT, UPDATE, DELETE ON rotacultural.* TO 'rc_app'@'%'");
                try (var appConnection = DriverManager.getConnection(url, "rc_app", appPassword)) {
                    System.out.println("Usuário rc_app validado com privilégios DML limitados ao schema.");
                }
            } else if (!mode.equals("audit")) {
                throw new IllegalArgumentException("Modo inválido");
            }
        } catch (Exception failure) {
            // Não imprimir SQL nem detalhes que possam conter credenciais.
            System.err.println("Falha no preparo do banco: " + failure.getClass().getSimpleName());
            System.exit(1);
        }
    }
}
