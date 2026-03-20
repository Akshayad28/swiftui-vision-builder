private static String extractOutlineId(Scenario scenario) {
    try {
        // ✅ Tags are SAME for all examples of one outline
        // e.g. [@XT-LH-3635, @XT-LH-3628, @monitorAccuracy]
        // and DIFFERENT for different outlines
        // e.g. [@XT-LH-3635, @XT-LH-3627, @monitorCompleteness]
        Collection<String> tags = scenario.getSourceTagNames();
        
        // Sort tags so order doesn't matter, join as single string key
        List<String> sortedTags = new ArrayList<>(tags);
        Collections.sort(sortedTags);
        String tagKey = String.join("_", sortedTags);
        
        System.out.println("Tag Key : " + tagKey); // debug
        
        return tagKey.isEmpty() ? "UnknownOutline" : tagKey;
        
    } catch (Exception e) {
        return "UnknownOutline";
    }
}


private static String extractOutlineName(Scenario scenario) {
    try {
        Collection<String> tags = scenario.getSourceTagNames();
        
        // ✅ Find the most specific tag (usually the last one like @monitorAccuracy)
        // Skip ticket tags like @XT-LH-XXXX
        for (String tag : tags) {
            if (!tag.startsWith("@XT-") && !tag.startsWith("XT-")) {
                return tag.replace("@", "").trim();
            }
        }
        
        // Fallback to full raw name if no suitable tag found
        String rawName = scenario.getName();
        if (rawName == null || rawName.trim().isEmpty()) {
            return "UnknownScenario";
        }
        String[] parts = rawName.split("\\s\\S+=");
        return parts[0].trim();
        
    } catch (Exception e) {
        return "UnknownScenario";
    }
}
