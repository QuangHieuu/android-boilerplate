pluginManagement {
	repositories {
		google {
			content {
				includeGroupByRegex("com\\.android.*")
				includeGroupByRegex("com\\.google.*")
				includeGroupByRegex("androidx.*")
			}
		}
		gradlePluginPortal()
		mavenCentral()
		maven { url = uri("https://jitpack.io") }
	}
}
@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		google {
			content {
				includeGroupByRegex("com\\.android.*")
				includeGroupByRegex("com\\.google.*")
				includeGroupByRegex("androidx.*")
			}
		}
		gradlePluginPortal()
		mavenCentral()
		maven { url = uri("https://jitpack.io") }
	}
}

rootProject.name = "Android_Boilerplate"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(
	":app",
	":refreshRecyclerView",
	":simpleRoundedImage",
	":compactShape",
	":simpleCalendar",
)
