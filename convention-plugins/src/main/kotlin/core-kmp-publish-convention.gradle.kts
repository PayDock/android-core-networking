plugins {
    id("maven-publish")
}

// artifact (from module build.gradle)
val groupName: String by project
val versionName: String by project
// artifact (from module build.gradle)
val libraryName: String by project
// project info (from module build.gradle)
val projectGithubUrl: String by project
val projectDescription: String by project
// developers (from gradle.properties)
val developerId: String by project
val developerName: String by project
val developerEmail: String by project
val developerOrganisation: String by project
val developerOrganisationUrl: String by project

group = groupName
version = versionName

afterEvaluate {
    publishing {
        publications {// Explicitly include iOS publications
            if (name.contains("ios", ignoreCase = true)) {
                group = groupName
                publications.withType<MavenPublication> {
                    groupId = groupName
                    artifactId = libraryName
                    version = versionName
                    // And here are some more properties that go into the pom file.
                    pom {
                        name.set(project.name)
                        description.set(projectDescription)
                        url.set(projectGithubUrl)
                        licenses {
                            license {
                                name.set("All Rights Reserved")
                                distribution.set("repo")
                            }
                        }
                        developers {
                            developer {
                                id.set(developerId)
                                name.set(developerName)
                                email.set(developerEmail)
                                organization.set(developerOrganisation)
                                organizationUrl.set(developerOrganisationUrl)
                            }
                        }
                        scm {
                            url.set(pom.url.get())
                            connection.set("scm:git:${url.get()}.git")
                            developerConnection.set("scm:git:${url.get()}.git")
                        }
                    }
                }
            }
        }
    }
}

// Task to print the version name used for pipeline
tasks.register("printVersionName") {
    doLast {
        println(versionName)
    }
}