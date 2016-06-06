package de.msg.osgiPluginToPackage;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class to search for manifest files.
 *
 * @author Dennis Rippinger
 */
public class SearchManifestFiles {

	private PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:**.MF");

	public List<Path> searchForFiles(Path baseDirectory) throws IOException {

		List<Path> collect = Files //
				.walk(baseDirectory) //
				.filter(this::filter) //
				.collect(Collectors.toList());

		return collect;
	}

	/**
	 * Filter for Manifest files and exclude such in target directories.
	 *
	 * @param file the current path.
	 * @return true if the filter criteria is met.
	 */
	public boolean filter(Path file) {

		return pathMatcher.matches(file) && //
				!file.toString().contains(File.separator + "target" + File.separator) && //
				!file.toString().contains(File.separator + ".metadata" + File.separator);
	}
}
