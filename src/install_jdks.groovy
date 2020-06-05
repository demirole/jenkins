import hudson.model.JDK
import hudson.tools.InstallSourceProperty
import hudson.tools.ZipExtractionInstaller

class InstallJDK {
    JDK.DescriptorImpl descriptor
    final javaTools = [
        ['name': 'jdk8',
         'url': 'https://github.com/AdoptOpenJDK/openjdk8-binaries/releases/download/jdk8u252-b09/OpenJDK8U-jdk_x64_linux_hotspot_8u252b09.tar.gz'],
        ['name': 'jdk11',
         'url': 'https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.7%2B10/OpenJDK11U-jdk_x64_linux_hotspot_11.0.7_10.tar.gz']
    ]

    static final String archiveDownloadDirectory = "${System.getenv('JENKINS_HOME')}/downloads"

    InstallJDK() {
        descriptor = new JDK.DescriptorImpl()
    }

    def process() {
        def installations = []

        javaTools.each { javaTool ->
            println("Setting up tool: ${javaTool.name}")
            def localFileName = downloadArchiveFromUrl(javaTool.url)
            def installer = new ZipExtractionInstaller(null, "file://${localFileName}", null)
            def jdk = new JDK(javaTool.name, null, [new InstallSourceProperty([installer])])
            installations.add(jdk)
        }

        descriptor.setInstallations(installations.toArray(new JDK[installations.size()]))
        descriptor.save()
    }

    private static String downloadArchiveFromUrl(String url) {
        File jdkArchive = new File("${archiveDownloadDirectory}/${getFilenameFromUrl(url)}")
        if (!jdkArchive.exists()) {
            jdkArchive.withOutputStream { out ->
                new URL(url).withInputStream { from ->
                    out << from
                    from.close()
                }
            }
        }
        return jdkArchive
    }

    private static String getFilenameFromUrl(String url) {
        URI uri = new URI(url);
        def path = uri.getPath();
        def returnValue = path.substring(path.lastIndexOf('/') + 1);
        return returnValue
    }
}

installJDK = new InstallJDK()
installJDK.process()