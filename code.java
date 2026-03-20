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
