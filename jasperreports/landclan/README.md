The jar file must be uploaded to the nexus. This can be done from the nexus web UI.

# Jaspersoft Studio

If you want to run Jaspersoft Studio version 6.21.2 using the LandClan custom version then you will need to follow these instructions.

1. Build jasperreports landclan version:
   * `mvn clean install`
   * `ant clean jar`
2. Launch Jaspersoft Studio 6.21.2.
3. Open a project (e.g. jaspersoft-studio-project).
4. In the "Project Explorer" window, expand the "JasperReports Library" tree.
5. Locate the entry for `jasperreports-6.21.2.jar` and read its file path.
6. Open the path in your file browser.
7. Close Jaspersoft Studio to free-up the jar file for editing.
8. Back-up the default version of this file by renaming it to `jasperreports-6.21.2.jar.official`.
9. Copy `dist/jasperreports-6.21.2-landclan.jar` into the directory and rename it `jasperreports-6.21.2.jar`.
