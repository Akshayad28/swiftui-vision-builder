// ✅ Capture tag (take first tag OR all tags)
        tagName = testScenario.getSourceTagNames()
                .stream()
                .map(tag -> tag.replace("@", ""))   // remove @
                .reduce((a, b) -> a + "_" + b)      // join if multiple
                .orElse("DefaultTag");

        // ✅ Clean invalid characters
        tagName = tagName.replaceAll("[\\\\/:*?\"<>|]", "_");

        System.out.println("📌 Captured Tag: " + tagName);
