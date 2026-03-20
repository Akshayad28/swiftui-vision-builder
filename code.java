private static String extractOutlineId(Scenario scenario) {
    try {
        List<String> sortedTags = new ArrayList<>(scenario.getSourceTagNames());
        Collections.sort(sortedTags);
        String tagKey = String.join("_", sortedTags);
        System.out.println("Tag Key : " + tagKey);
        return tagKey.isEmpty() ? "UnknownOutline" : tagKey;
    } catch (Exception e) {
        return "UnknownOutline";
    }
}

private static String extractOutlineName(Scenario scenario) {
    try {
        for (String tag : scenario.getSourceTagNames()) {
            // ✅ Tags confirmed with @ prefix so check exactly @XT-
            if (!tag.startsWith("@XT-")) {
                return tag.replace("@", "").trim();
                // Returns e.g. "monitorAccuracy" or "monitorCompleteness"
            }
        }
        return "UnknownScenario";
    } catch (Exception e) {
        return "UnknownScenario";
    }
}
