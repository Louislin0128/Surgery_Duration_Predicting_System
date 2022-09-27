package guiFunction;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

public class ZipOrUnzip {
	/**
	 * 壓縮
	 * @param source 來源
	 * @param target 目的
	 * @throws IOException
	 */
	public static void zip(Path source, Path target) throws IOException {
		// XXX 暫以複製完整目錄代替
		Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				Files.createDirectory(target.resolve(source.relativize(dir)));
				return FileVisitResult.CONTINUE;
			}
			
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.copy(file, target.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
				return FileVisitResult.CONTINUE;
			}
		});
//		try(ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(target))) {
//			Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
//		    	@Override
//		    	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//		    		ZipEntry zipEntry = new ZipEntry(source.relativize(file).toString());
//					zs.putNextEntry(zipEntry);
//					Files.copy(file, zs);
//					zs.closeEntry();
//		    		return FileVisitResult.CONTINUE;
//		    	}
//		    });
//		}
	}
	
	/**
	 * 解壓縮
	 * @param source 來源
	 * @param target 目的
	 * @throws IOException
	 */
	public static void unzip(Path source, Path target) throws IOException {
		// XXX 暫以直接設定來源代替
		Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.copy(file, target.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
				return FileVisitResult.CONTINUE;
			}
		});
//		try(ZipInputStream zis = new ZipInputStream(Files.newInputStream(source))){
//			ZipEntry zipEntry;
//	        Path newPath, parentPath;
//	        while ((zipEntry = zis.getNextEntry()) != null) {
//	            newPath = zipSlipProtect(zipEntry, target);
//	            
//	            if (zipEntry.isDirectory()) {
//	                Files.createDirectories(newPath);
//	            }else {
//	                if ((parentPath = newPath.getParent()) != null) {
//	                    if (Files.notExists(parentPath)) {
//	                        Files.createDirectories(parentPath);
//	                    }
//	                }
//	                Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
//	            }
//	        }
//		}
    }
	
//	private static Path zipSlipProtect(ZipEntry zipEntry, Path targetDir) throws IOException {
//		Path targetDirResolved = targetDir.resolve(zipEntry.getName());
//	    Path normalizePath = targetDirResolved.normalize();
//	    if (!normalizePath.startsWith(targetDir)) {
//	       throw new IOException("Bad zip entry: " + zipEntry.getName());
//	    }
//	    return normalizePath;
//	}
}
