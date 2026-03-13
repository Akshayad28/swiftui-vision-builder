// Load resource to ensure it exists inside jar
        InputStream stream =
                DBSQL.class.getClassLoader().getResourceAsStream(DB_PROPERTIES);

        if (stream == null) {
            throw new RuntimeException("❌ application-preprod.yaml not found in classpath");
        
