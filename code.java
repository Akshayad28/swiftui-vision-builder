public static void writeData() {

    FileOutputStream fos = null;

    try {

        if (workbook == null) {
            System.out.println("⚠ Workbook is null, nothing to write");
            return;
        }

        fos = new FileOutputStream(file);

        // 👉 This writes EXISTING workbook (no reset)
        workbook.write(fos);

        System.out.println("📁 Excel file written successfully: " + file.getAbsolutePath());

    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        try {
            if (fos != null) fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
