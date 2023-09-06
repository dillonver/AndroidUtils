package xyz.dcln.androidutils.utils


import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Created by dcl on 2023/7/7.
 */
object ZipUtils {

    /**
     * Zips a single file.
     *
     * @param sourceFilePath The absolute path of the file to be zipped.
     * @param destinationZipPath The absolute path where the zipped file should be saved.
     */
    fun zipFile(sourceFilePath: String, destinationZipPath: String) {
        FileInputStream(sourceFilePath).use { fis ->
            FileOutputStream(destinationZipPath).use { fos ->
                ZipOutputStream(BufferedOutputStream(fos)).use { zos ->
                    val buffer = ByteArray(1024)
                    var length: Int
                    zos.putNextEntry(ZipEntry(sourceFilePath))
                    while (fis.read(buffer).also { length = it } > 0) {
                        zos.write(buffer, 0, length)
                    }
                    zos.closeEntry()
                }
            }
        }
    }

    /**
     * Zips an entire directory. Note: Doesn't support recursive zipping of subdirectories.
     *
     * @param sourceDirPath The absolute path of the directory to be zipped.
     * @param destinationZipPath The absolute path where the zipped directory should be saved.
     */
    fun zipDirectory(sourceDirPath: String, destinationZipPath: String) {
        val srcDir = java.io.File(sourceDirPath)
        FileOutputStream(destinationZipPath).use { fos ->
            ZipOutputStream(BufferedOutputStream(fos)).use { zos ->
                val buffer = ByteArray(1024)
                for (file in srcDir.listFiles()!!) {
                    FileInputStream(file).use { fis ->
                        val zipEntry = ZipEntry(file.name)
                        zos.putNextEntry(zipEntry)
                        var length: Int
                        while (fis.read(buffer).also { length = it } > 0) {
                            zos.write(buffer, 0, length)
                        }
                        zos.closeEntry()
                    }
                }
            }
        }
    }

    /**
     * Unzips a zipped file into a specified directory.
     *
     * @param sourceZipPath The absolute path of the zipped file to be unzipped.
     * @param destinationDirPath The absolute path of the directory where the unzipped contents should be saved.
     */
    fun unzip(sourceZipPath: String, destinationDirPath: String) {
        FileInputStream(sourceZipPath).use { fis ->
            BufferedInputStream(fis).use { bis ->
                ZipInputStream(bis).use { zis ->
                    var zipEntry: ZipEntry? = zis.nextEntry
                    val buffer = ByteArray(1024)
                    while (zipEntry != null) {
                        val newFile = java.io.File(destinationDirPath, zipEntry.name)
                        FileOutputStream(newFile).use { fos ->
                            var length: Int
                            while (zis.read(buffer).also { length = it } > 0) {
                                fos.write(buffer, 0, length)
                            }
                        }
                        zipEntry = zis.nextEntry
                    }
                }
            }
        }
    }
}
