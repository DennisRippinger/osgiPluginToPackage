package de.msg.osgiPluginToPackage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import org.junit.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Converter {

	private static final String PLUGIN = "<- YOUR PLUGIN (SUFFIX) ->";
	public static final String PATH = "<-YOUR PATH ->";

	public static final String PROP_REQUIRE_BUNDLE = "Require-Bundle";

	public SearchManifestFiles searchManifestFiles = new SearchManifestFiles();

	@Test
	public void movePluginsToAdditionalBundles() throws IOException {

		List<Path> manifestFiles = searchManifestFiles.searchForFiles(Paths.get(PATH));

		for (Path manifestFile : manifestFiles) {
			Manifest manifest = new Manifest(new FileInputStream(manifestFile.toFile()));

			List<String> requiredPlugins = filterRequiredPlugins(manifest);

			Path buildPropertiesPath = createBuildPropertiesPath(manifestFile);

			if (!buildPropertiesPath.toFile().exists() || requiredPlugins.isEmpty())
				continue;

			Properties buildProperties = new Properties();
			buildProperties.load(new FileInputStream(buildPropertiesPath.toFile()));

			String additionBundles = (String) buildProperties.get("additional.bundles");
			if (additionBundles == null) {
				additionBundles = "";
			}

			additionBundles += requiredPlugins.stream().collect(Collectors.joining(","));

			removeRequiredPluginsFromManifest(manifest, requiredPlugins);
			buildProperties.put("additional.bundles", additionBundles);

			buildProperties.store(new FileOutputStream(buildPropertiesPath.toFile()), null);
			manifest.write(new FileOutputStream(manifestFile.toFile()));
		}

	}

	private void removeRequiredPluginsFromManifest(Manifest manifest, List<String> requiredPlugins) {
		String exportPackage = manifest.getMainAttributes().getValue(PROP_REQUIRE_BUNDLE);
		exportPackage = Arrays//
				.asList(exportPackage.split(","))//
				.stream()//
				.filter(x -> !requiredPlugins.contains(x))//
				.collect(Collectors.toList())//
				.stream()//
				.collect(Collectors.joining(","));

		if ("".equals(exportPackage)) {
			manifest.getMainAttributes().remove(new Attributes.Name(PROP_REQUIRE_BUNDLE));
		} else {
			manifest.getMainAttributes().put(new Attributes.Name(PROP_REQUIRE_BUNDLE), exportPackage);
		}
	}

	/**
	 * Basically cd ../build.properties
	 *
	 * @param manifestFile location of the manifest file.
	 * @return path to build.properties.
	 */
	private Path createBuildPropertiesPath(Path manifestFile) {
		return Paths.get(manifestFile.getParent().getParent() + File.separator + "build.properties");
	}

	private List<String> filterRequiredPlugins(Manifest manifest) {

		String exportPackage = manifest.getMainAttributes().getValue(PROP_REQUIRE_BUNDLE);

		if (exportPackage == null)
			return Collections.emptyList();

		String[] packages = exportPackage.split(",");

		return Arrays.asList(packages)//
				.stream()//
				.filter(x -> x.contains(PLUGIN))//
				.collect(Collectors.toList());
	}
}
