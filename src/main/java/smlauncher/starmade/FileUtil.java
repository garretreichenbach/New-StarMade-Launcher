package smlauncher.starmade;

import org.apache.commons.io.output.StringBuilderWriter;
import smlauncher.LogManager;

import java.io.*;
import java.net.*;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileUtil {

	/**
	 * The Unix directory separator character.
	 */
	public static final char DIR_SEPARATOR_UNIX = '/';
	/**
	 * The Windows directory separator character.
	 */
	public static final char DIR_SEPARATOR_WINDOWS = '\\';
	/**
	 * The system directory separator character.
	 */
	public static final char DIR_SEPARATOR = File.separatorChar;
	/**
	 * The Unix line separator string.
	 */
	public static final String LINE_SEPARATOR_UNIX = "\n";
	/**
	 * The Windows line separator string.
	 */
	public static final String LINE_SEPARATOR_WINDOWS = "\r\n";
	/**
	 * The system line separator string.
	 */
	public static final String LINE_SEPARATOR;
	private static final int EOF = -1;
	/**
	 * The default buffer size ({@value}) to use for
	 */
	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

	static {
		// avoid security issues
		StringBuilderWriter buf = new StringBuilderWriter(4);
		PrintWriter out = new PrintWriter(buf);
		out.println();
		LINE_SEPARATOR = buf.toString();
		out.close();
	}

	public static void copyDirectory(File sourceLocation, File targetLocation) throws IOException {
		//		System.err.println("DIR FROM " + sourceLocation.getAbsolutePath() + " to " + targetLocation.getAbsolutePath());
		if(sourceLocation.isDirectory()) {
			if(!targetLocation.exists()) {
				targetLocation.mkdir();
			}

			String[] children = sourceLocation.list();
			for(int i = 0; i < children.length; i++) {

				copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
			}
		} else {
			copyFile(sourceLocation, targetLocation);
		}
	}

	public static URL convertToURLEscapingIllegalCharacters(String string) throws UnsupportedEncodingException, URISyntaxException, MalformedURLException {
		String decodedURL = URLDecoder.decode(string);
		URL url = new URL(decodedURL);
		URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
		return uri.toURL();
	}

	public static void copyFileIfDifferentPath(File sourceLocation, File targetLocation) throws IOException {
		if(!sourceLocation.getCanonicalPath().equals(targetLocation.getCanonicalPath())) {
			copyFile(sourceLocation, targetLocation);
		} else {
			System.err.println("[IO][COPY] (NO COPY SINCE SAME PATH) FILE FROM " + sourceLocation.getAbsolutePath() + " to " + targetLocation.getAbsolutePath());
		}
	}

	public static void copyFile(File sourceLocation, File targetLocation) throws IOException {
		System.err.println("[IO][COPY] FILE FROM " + sourceLocation.getAbsolutePath() + " to " + targetLocation.getAbsolutePath());
		InputStream in = new BufferedInputStream(new FileInputStream(sourceLocation));
		OutputStream out = new BufferedOutputStream(new FileOutputStream(targetLocation));

		// Copy the bits from instream to outstream
		byte[] buf = new byte[1024];
		int len;
		while((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		out.flush();
		in.close();
		out.close();
	}

	public static final void copyInputStream(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int len;
		while((len = in.read(buffer)) >= 0) out.write(buffer, 0, len);
		in.close();
		out.close();
	}

	public static File createTempDirectory(String name) throws IOException {
		File temp;

		temp = File.createTempFile(name, Long.toString(System.nanoTime()));

		if(!(temp.delete())) {
			throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
		}

		if(!(temp.mkdir())) {
			throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
		}

		return (temp);
	}

	public static void deleteRecursive(File f) throws IOException {
		if(f.isDirectory()) {
			for(File c : f.listFiles()) {
				deleteRecursive(c);
			}
		}
		boolean delete = f.delete();
		if(!delete) {
			System.err.println("Failed to del: " + f.getAbsolutePath());
		} else {
			//			System.err.println("Successfully deleted: "+f.getAbsolutePath());
		}
	}

	public static boolean deleteDir(File dir) {
		if(dir.isDirectory()) {
			String[] children = dir.list();
			for(int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if(!success) {
					return false;
				}
			}
		}

		// The directory is now empty so delete it
		return dir.delete();
	}

	public static void extract(File zip, String installPath) throws IOException {
		extract(zip, installPath, null, null);
	}

	public static long getExtractedFilesSize(File zip) throws IOException {
		long size = 0;
		ZipFile zipFile = new ZipFile(zip);

		try {
			Enumeration<?> e = zipFile.entries();
			while(e.hasMoreElements()) {
				ZipEntry ze = (ZipEntry) e.nextElement();
				long uncompressedSize = ze.getSize();
				//				long compressedSize = ze.getCompressedSize();
				//				System.err.println("FILE SIZE: "+ze.getName()+": "+uncompressedSize);
				size += uncompressedSize;
			}
		} finally {
			zipFile.close();
		}
		return size;
	}

	public static void extract(File zip, String installPath, String replace, FolderZipper.ZipCallback callback) throws IOException {
		if(!installPath.endsWith("/")) {
			installPath += "/";
		}
		ZipFile zipFile = new ZipFile(zip);

		{
			Enumeration<? extends ZipEntry> entries = zipFile.entries();

			File f = new File(installPath);
			f.mkdirs();

			//			while (entries.hasMoreElements()) {
			//				System.err.println("zip ENTRY: "+entries.nextElement().getName());
			//			}
		}
		Enumeration<? extends ZipEntry> entries = zipFile.entries();

		File f = new File(installPath);
		f.mkdirs();
		while(entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			if(entry.isDirectory()) {
				// Assume directories are stored parents first then children.

				//				System.err.println("Extracting directory: " + entry.getName());
				// This is not robust, just for demonstration purposes.

				if(replace != null) {
					(new File(installPath + entry.getName().replaceFirst(replace, ""))).mkdir();
				} else {
					(new File(installPath + entry.getName())).mkdir();
				}

				continue;
			}
			String s = entry.getName();

			if(replace != null) {
				s = entry.getName().replaceFirst(replace, "");
			} else {
				s = entry.getName();
			}

			int i = s.lastIndexOf('/');
			//			System.err.println("INDEX: "+i+" "+entry.getName()+" -> "+"/");
			if(i >= 0) {
				String path = s.substring(0, i);
				File p = new File(installPath + path);
				if(!p.exists()) {
					//					System.err.println("MKDIRS: "+p.getAbsolutePath());
					p.mkdirs();
				}
			}

			File extr;

			if(replace != null) {
				extr = new File(installPath + entry.getName().replaceFirst(replace, ""));
			} else {
				extr = new File(installPath + entry.getName());
			}

			if(callback != null) {
				callback.update(extr);
			}
			System.err.println("Extracting file: " + entry.getName() + " exists: " + f.exists() + ", is Dir: " + f.isDirectory() + ". " + f.getAbsolutePath());
			BufferedInputStream bufferedInputStream = new BufferedInputStream(zipFile.getInputStream(entry));
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(extr));
			copyInputStream(bufferedInputStream, bufferedOutputStream);

		}
		zipFile.close();
	}

	public static void main(String[] sdf) {
		try {
			extract(new File("./sector-export/planet.smsec"), "./sector-export/mm/");
			deleteRecursive(new File("./sector-export/mm/"));
			System.err.println((new File("./sector-export/mm/")).delete());

		} catch(IOException e) {
			LogManager.logWarning("Failed to extract", e);
		}
	}

	public static void backupFile(File f) throws IOException {
		System.err.println("BACKING UP FILE: " + f.getAbsolutePath());
		String fname = f.getName();
		int v = 0;

		while((new File(fname)).exists()) {
			fname = f.getName() + "." + v;
			v++;
		}
		System.err.println("BACKING UP FILE TO: " + f.getAbsolutePath() + " -> " + fname);
		copyFile(f, new File(fname));
	}

	/**
	 * Copies bytes from the URL {@code source} to a file
	 * {@code destination}. The directories up to {@code destination}
	 * will be created if they don't already exist. {@code destination}
	 * will be overwritten if it already exists.
	 *
	 * @param source            the {@code URL} to copy bytes from, must not be {@code null}
	 * @param destination       the non-directory {@code File} to write bytes to
	 *                          (possibly overwriting), must not be {@code null}
	 * @param connectionTimeout the number of milliseconds until this method
	 *                          will timeout if no connection could be established to the {@code source}
	 * @param readTimeout       the number of milliseconds until this method will
	 *                          timeout if no data could be read from the {@code source}
	 * @throws IOException if {@code source} URL cannot be opened
	 * @throws IOException if {@code destination} is a directory
	 * @throws IOException if {@code destination} cannot be written
	 * @throws IOException if {@code destination} needs creating but can't be
	 * @throws IOException if an IO error occurs during copying
	 * @since 2.0
	 */
	public static void copyURLToFile(URL source, File destination, int connectionTimeout, int readTimeout, DownloadCallback cb) throws IOException {
		URLConnection connection = source.openConnection();
		connection.setConnectTimeout(connectionTimeout);
		connection.setReadTimeout(readTimeout);
		InputStream input = connection.getInputStream();
		copyInputStreamToFile(input, destination, cb);
	}

	public static void copyURLToFile(URL source, File destination, int connectionTimeout, int readTimeout, DownloadCallback cb, String user, String pass, boolean resume) throws IOException {
		URLConnection connection = null;
		try {
			connection = source.openConnection();
			connection.setConnectTimeout(connectionTimeout);
			connection.setReadTimeout(readTimeout);
			// #RM1958 swap out Base64 encoder
			//			String encoding = Base64.encode((user+":"+pass).getBytes());
			//			connection.setRequestProperty ("Authorization", "Basic " + encoding);
			InputStream input;

			if(resume) {
				if(destination.exists()) {
					long downloadedSize = destination.length();
					connection.setAllowUserInteraction(true);
					connection.setRequestProperty("Range", "bytes=" + downloadedSize + "-");
					connection.setConnectTimeout(14000);
					input = new BufferedInputStream(connection.getInputStream());
					input.skip(downloadedSize); //Skip downloaded size
				} else {
					input = new BufferedInputStream(connection.getInputStream());
				}

			} else {
				input = new BufferedInputStream(connection.getInputStream());
			}

			copyInputStreamToFile(input, destination, cb);
		} catch(IOException e) {
			if(resume) {
				try {
					System.err.println("Disconnected: " + e.getClass() + ": " + e.getMessage() + "! Trying to resume download!");
					Thread.sleep(1000);
				} catch(InterruptedException e1) {
					LogManager.logWarning("Interrupted while waiting to resume download", e1);
				}
				copyURLToFile(source, destination, connectionTimeout, readTimeout, cb, user, pass, resume);
			} else {
				throw e;
			}
		}
	}

	/**
	 * Copies bytes from an {@link InputStream} {@code source} to a file
	 * {@code destination}. The directories up to {@code destination}
	 * will be created if they don't already exist. {@code destination}
	 * will be overwritten if it already exists.
	 *
	 * @param source      the {@code InputStream} to copy bytes from, must not be {@code null}
	 * @param destination the non-directory {@code File} to write bytes to
	 *                    (possibly overwriting), must not be {@code null}
	 * @throws IOException if {@code destination} is a directory
	 * @throws IOException if {@code destination} cannot be written
	 * @throws IOException if {@code destination} needs creating but can't be
	 * @throws IOException if an IO error occurs during copying
	 * @since 2.0
	 */
	public static void copyInputStreamToFile(InputStream source, File destination, DownloadCallback cb) throws IOException {
		try {
			FileOutputStream output = openOutputStream(destination);
			try {
				copy(source, output, cb);
				output.close(); // don't swallow close Exception if copy completes normally
			} finally {
				closeQuietly(output);
			}
		} finally {
			closeQuietly(source);
		}
	}
	// copy from InputStream
	//-----------------------------------------------------------------------

	/**
	 * Copy bytes from an {@code InputStream} to an
	 * {@code OutputStream}.
	 * <p/>
	 * This method buffers the input internally, so there is no need to use a
	 * {@code BufferedInputStream}.
	 * <p/>
	 * Large streams (over 2GB) will return a bytes copied value of
	 * {@code -1} after the copy has completed since the correct
	 * number of bytes cannot be returned as an int. For large streams
	 * use the {@code copyLarge(InputStream, OutputStream)} method.
	 *
	 * @param input  the {@code InputStream} to read from
	 * @param output the {@code OutputStream} to write to
	 * @return the number of bytes copied, or -1 if &gt; Integer.MAX_VALUE
	 * @throws NullPointerException if the input or output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 1.1
	 */
	public static int copy(InputStream input, OutputStream output, DownloadCallback cb) throws IOException {
		long count = copyLarge(input, output, cb);
		if(count > Integer.MAX_VALUE) {
			return -1;
		}
		return (int) count;
	}

	/**
	 * Scans all classes accessible from the context class loader which belong
	 * to the given package and subpackages.
	 *
	 * @param packageName The base package
	 * @return The classes
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public static Iterable<Class> getClasses(String packageName) throws ClassNotFoundException, IOException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = classLoader.getResources(path);
		List<File> dirs = new ArrayList<File>();
		while(resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			dirs.add(new File(resource.getFile()));
		}
		List<Class> classes = new ArrayList<Class>();
		for(File directory : dirs) {
			classes.addAll(findClasses(directory, packageName));
		}

		return classes;
	}

	/**
	 * Recursive method used to find all classes in a given directory and
	 * subdirs.
	 *
	 * @param directory   The base directory
	 * @param packageName The package name for classes found inside the base directory
	 * @return The classes
	 * @throws ClassNotFoundException
	 */
	private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
		List<Class> classes = new ArrayList<Class>();
		if(!directory.exists()) {
			return classes;
		}
		File[] files = directory.listFiles();
		for(File file : files) {
			if(file.isDirectory()) {
				classes.addAll(findClasses(file, packageName + "." + file.getName()));
			} else if(file.getName().endsWith(".class")) {
				classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
			}
		}
		return classes;
	}

	/**
	 * Opens a {@link FileOutputStream} for the specified file, checking and
	 * creating the parent directory if it does not exist.
	 * <p/>
	 * At the end of the method either the stream will be successfully opened,
	 * or an exception will have been thrown.
	 * <p/>
	 * The parent directory will be created if it does not exist.
	 * The file will be created if it does not exist.
	 * An exception is thrown if the file object exists but is a directory.
	 * An exception is thrown if the file exists but cannot be written to.
	 * An exception is thrown if the parent directory cannot be created.
	 *
	 * @param file the file to open for output, must not be {@code null}
	 * @return a new {@link FileOutputStream} for the specified file
	 * @throws IOException if the file object is a directory
	 * @throws IOException if the file cannot be written to
	 * @throws IOException if a parent directory needs creating but that fails
	 * @since 1.3
	 */
	public static FileOutputStream openOutputStream(File file) throws IOException {
		return openOutputStream(file, false);
	}

	/**
	 * Opens a {@link FileOutputStream} for the specified file, checking and
	 * creating the parent directory if it does not exist.
	 * <p/>
	 * At the end of the method either the stream will be successfully opened,
	 * or an exception will have been thrown.
	 * <p/>
	 * The parent directory will be created if it does not exist.
	 * The file will be created if it does not exist.
	 * An exception is thrown if the file object exists but is a directory.
	 * An exception is thrown if the file exists but cannot be written to.
	 * An exception is thrown if the parent directory cannot be created.
	 *
	 * @param file   the file to open for output, must not be {@code null}
	 * @param append if {@code true}, then bytes will be added to the
	 *               end of the file rather than overwriting
	 * @return a new {@link FileOutputStream} for the specified file
	 * @throws IOException if the file object is a directory
	 * @throws IOException if the file cannot be written to
	 * @throws IOException if a parent directory needs creating but that fails
	 * @since 2.1
	 */
	public static FileOutputStream openOutputStream(File file, boolean append) throws IOException {
		if(file.exists()) {
			if(file.isDirectory()) {
				throw new IOException("File '" + file + "' exists but is a directory");
			}
			if(!file.canWrite()) {
				throw new IOException("File '" + file + "' cannot be written to");
			}
		} else {
			File parent = file.getParentFile();
			if(parent != null) {
				if(!parent.mkdirs() && !parent.isDirectory()) {
					throw new IOException("Directory '" + parent + "' could not be created");
				}
			}
		}
		return new FileOutputStream(file, append);
	}
	//-----------------------------------------------------------------------

	/**
	 * Closes a URLConnection.
	 *
	 * @param conn the connection to close.
	 * @since 2.4
	 */
	public static void close(URLConnection conn) {
		if(conn instanceof HttpURLConnection) {
			((HttpURLConnection) conn).disconnect();
		}
	}

	/**
	 * Unconditionally close an {@code Reader}.
	 * <p/>
	 * Equivalent to {@link Reader#close()}, except any exceptions will be ignored.
	 * This is typically used in finally blocks.
	 * <p/>
	 * Example code:
	 * <pre>
	 *   char[] data = new char[1024];
	 *   Reader in = null;
	 *   try {
	 *       in = new FileReader("foo.txt");
	 *       in.read(data);
	 *       in.close(); //close errors are handled
	 *   } catch (Exception e) {
	 *       // error handling
	 *   } finally {
	 *       IOUtils.closeQuietly(in);
	 *   }
	 * </pre>
	 *
	 * @param input the Reader to close, may be null or already closed
	 */
	public static void closeQuietly(Reader input) {
		closeQuietly((Closeable) input);
	}

	/**
	 * Unconditionally close a {@code Writer}.
	 * <p/>
	 * Equivalent to {@link Writer#close()}, except any exceptions will be ignored.
	 * This is typically used in finally blocks.
	 * <p/>
	 * Example code:
	 * <pre>
	 *   Writer out = null;
	 *   try {
	 *       out = new StringWriter();
	 *       out.write("Hello World");
	 *       out.close(); //close errors are handled
	 *   } catch (Exception e) {
	 *       // error handling
	 *   } finally {
	 *       IOUtils.closeQuietly(out);
	 *   }
	 * </pre>
	 *
	 * @param output the Writer to close, may be null or already closed
	 */
	public static void closeQuietly(Writer output) {
		closeQuietly((Closeable) output);
	}

	/**
	 * Unconditionally close an {@code InputStream}.
	 * <p/>
	 * Equivalent to {@link InputStream#close()}, except any exceptions will be ignored.
	 * This is typically used in finally blocks.
	 * <p/>
	 * Example code:
	 * <pre>
	 *   byte[] data = new byte[1024];
	 *   InputStream in = null;
	 *   try {
	 *       in = new FileInputStream("foo.txt");
	 *       in.read(data);
	 *       in.close(); //close errors are handled
	 *   } catch (Exception e) {
	 *       // error handling
	 *   } finally {
	 *       IOUtils.closeQuietly(in);
	 *   }
	 * </pre>
	 *
	 * @param input the InputStream to close, may be null or already closed
	 */
	public static void closeQuietly(InputStream input) {
		closeQuietly((Closeable) input);
	}

	/**
	 * Unconditionally close an {@code OutputStream}.
	 * <p/>
	 * Equivalent to {@link OutputStream#close()}, except any exceptions will be ignored.
	 * This is typically used in finally blocks.
	 * <p/>
	 * Example code:
	 * <pre>
	 * byte[] data = "Hello, World".getBytes();
	 *
	 * OutputStream out = null;
	 * try {
	 *     out = new FileOutputStream("foo.txt");
	 *     out.write(data);
	 *     out.close(); //close errors are handled
	 * } catch (IOException e) {
	 *     // error handling
	 * } finally {
	 *     IOUtils.closeQuietly(out);
	 * }
	 * </pre>
	 *
	 * @param output the OutputStream to close, may be null or already closed
	 */
	public static void closeQuietly(OutputStream output) {
		closeQuietly((Closeable) output);
	}

	/**
	 * Unconditionally close a {@code Closeable}.
	 * <p/>
	 * Equivalent to {@link Closeable#close()}, except any exceptions will be ignored.
	 * This is typically used in finally blocks.
	 * <p/>
	 * Example code:
	 * <pre>
	 *   Closeable closeable = null;
	 *   try {
	 *       closeable = new FileReader("foo.txt");
	 *       // process closeable
	 *       closeable.close();
	 *   } catch (Exception e) {
	 *       // error handling
	 *   } finally {
	 *       IOUtils.closeQuietly(closeable);
	 *   }
	 * </pre>
	 *
	 * @param closeable the object to close, may be null or already closed
	 * @since 2.0
	 */
	public static void closeQuietly(Closeable closeable) {
		try {
			if(closeable != null) {
				closeable.close();
			}
		} catch(IOException ioe) {
			// ignore
		}
	}

	/**
	 * Unconditionally close a {@code Socket}.
	 * <p/>
	 * Equivalent to {@link Socket#close()}, except any exceptions will be ignored.
	 * This is typically used in finally blocks.
	 * <p/>
	 * Example code:
	 * <pre>
	 *   Socket socket = null;
	 *   try {
	 *       socket = new Socket("http://www.foo.com/", 80);
	 *       // process socket
	 *       socket.close();
	 *   } catch (Exception e) {
	 *       // error handling
	 *   } finally {
	 *       IOUtils.closeQuietly(socket);
	 *   }
	 * </pre>
	 *
	 * @param sock the Socket to close, may be null or already closed
	 * @since 2.0
	 */
	public static void closeQuietly(Socket sock) {
		if(sock != null) {
			try {
				sock.close();
			} catch(IOException ioe) {
				// ignored
			}
		}
	}

	/**
	 * Unconditionally close a {@code Selector}.
	 * <p/>
	 * Equivalent to {@link Selector#close()}, except any exceptions will be ignored.
	 * This is typically used in finally blocks.
	 * <p/>
	 * Example code:
	 * <pre>
	 *   Selector selector = null;
	 *   try {
	 *       selector = Selector.open();
	 *       // process socket
	 *
	 *   } catch (Exception e) {
	 *       // error handling
	 *   } finally {
	 *       IOUtils.closeQuietly(selector);
	 *   }
	 * </pre>
	 *
	 * @param selector the Selector to close, may be null or already closed
	 * @since 2.2
	 */
	public static void closeQuietly(Selector selector) {
		if(selector != null) {
			try {
				selector.close();
			} catch(IOException ioe) {
				// ignored
			}
		}
	}

	/**
	 * Unconditionally close a {@code ServerSocket}.
	 * <p/>
	 * Equivalent to {@link ServerSocket#close()}, except any exceptions will be ignored.
	 * This is typically used in finally blocks.
	 * <p/>
	 * Example code:
	 * <pre>
	 *   ServerSocket socket = null;
	 *   try {
	 *       socket = new ServerSocket();
	 *       // process socket
	 *       socket.close();
	 *   } catch (Exception e) {
	 *       // error handling
	 *   } finally {
	 *       IOUtils.closeQuietly(socket);
	 *   }
	 * </pre>
	 *
	 * @param sock the ServerSocket to close, may be null or already closed
	 * @since 2.2
	 */
	public static void closeQuietly(ServerSocket sock) {
		if(sock != null) {
			try {
				sock.close();
			} catch(IOException ioe) {
				// ignored
			}
		}
	}

	/**
	 * Copy bytes from a large (over 2GB) {@code InputStream} to an
	 * {@code OutputStream}.
	 * <p/>
	 * This method buffers the input internally, so there is no need to use a
	 * {@code BufferedInputStream}.
	 * <p/>
	 * The buffer size is given by {@link #DEFAULT_BUFFER_SIZE}.
	 *
	 * @param input  the {@code InputStream} to read from
	 * @param output the {@code OutputStream} to write to
	 * @return the number of bytes copied
	 * @throws NullPointerException if the input or output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 1.3
	 */
	public static long copyLarge(InputStream input, OutputStream output, DownloadCallback cb) throws IOException {
		return copyLarge(input, output, new byte[DEFAULT_BUFFER_SIZE], cb);
	}

	/**
	 * Copy bytes from a large (over 2GB) {@code InputStream} to an
	 * {@code OutputStream}.
	 * <p/>
	 * This method uses the provided buffer, so there is no need to use a
	 * {@code BufferedInputStream}.
	 * <p/>
	 *
	 * @param input  the {@code InputStream} to read from
	 * @param output the {@code OutputStream} to write to
	 * @param buffer the buffer to use for the copy
	 * @return the number of bytes copied
	 * @throws NullPointerException if the input or output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 2.2
	 */
	public static long copyLarge(InputStream input, OutputStream output, byte[] buffer, DownloadCallback cb) throws IOException {
		long count = 0;
		int n = 0;
		while(EOF != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
			if(cb != null) {
				cb.downloaded(count, n);
			}
		}
		if(cb != null) {
			cb.doneDownloading();
		}
		return count;
	}

	public static byte[] createChecksum(File filename) throws IOException {
		InputStream fis = new BufferedInputStream(new FileInputStream(filename));

		byte[] buffer = new byte[1024];
		MessageDigest complete;
		try {
			complete = MessageDigest.getInstance("SHA1");

			int numRead;

			while((numRead = fis.read(buffer)) != -1) {
				complete.update(buffer, 0, numRead);
			}

			return complete.digest();
		} catch(NoSuchAlgorithmException e) {
			throw new IOException(e);
		} finally {
			fis.close();
		}
	}

	public static byte[] createChecksum(String filename) throws NoSuchAlgorithmException, IOException {
		return createChecksum(new File(filename));
	}

	public static byte[] createChecksumZipped(String filename) throws IOException {
		GZIPInputStream fis = new GZIPInputStream(new BufferedInputStream(new FileInputStream(filename)));

		byte[] buffer = new byte[1024];
		MessageDigest complete;
		try {
			complete = MessageDigest.getInstance("SHA1");

			int numRead;

			while((numRead = fis.read(buffer)) != -1) {
				complete.update(buffer, 0, numRead);
			}

			return complete.digest();
		} catch(NoSuchAlgorithmException e) {
			throw new IOException(e);
		} finally {
			fis.close();
		}
	}

	// see this How-to for a faster way to convert
	// a byte array to a HEX string
	public static String getSha1Checksum(String filename) throws IOException {
		byte[] b = new byte[0];
		try {
			b = createChecksum(filename);
		} catch(NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		String result = "";

		for(int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}

	// see this How-to for a faster way to convert
	// a byte array to a HEX string
	public static String getSha1ChecksumZipped(String filename) throws IOException {
		byte[] b = createChecksumZipped(filename);
		String result = "";

		for(int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}

	public static String getSha1Checksum(File filename) throws IOException {
		byte[] b = createChecksum(filename);
		String result = "";

		for(int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}

	public static String fileToString(File file) throws IOException {

		FileReader s = null;
		try {
			s = new FileReader(file, StandardCharsets.UTF_8);
			BufferedReader r = new BufferedReader(s);
			StringBuffer sb = new StringBuffer();

			String line;
			while((line = r.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}

			return sb.toString();
		} finally {
			if(s != null) {
				s.close();
			}
		}

	}

	public static int countFilesRecusrively(String path) {
		File f = new File(path);
		int c = 0;
		File[] listFiles = f.listFiles();
		if(listFiles != null) {
			for(int i = 0; i < listFiles.length; i++) {
				if(listFiles[i].isDirectory()) {
					c += countFilesRecusrively(listFiles[i].getAbsolutePath());
					c++;
				} else {
					c++;
				}
			}
		}
		return c;
	}

	private static void createFilesHashRecursively(String path, FileFilter fn, ArrayList<File> files) throws IOException {

		File f = new File(path);
		if(f.exists() && f.isDirectory()) {
			File[] listFiles = f.listFiles();
			if(listFiles != null) {
				for(File listFile : listFiles) {
					if(listFile.isDirectory()) {
						if(fn.accept(listFile)) createFilesHashRecursively(listFile.getAbsolutePath(), fn, files);
					} else if(fn.accept(listFile)) files.add(listFile);
				}
			}
		}
	}

	public static String createFilesHashRecursively(String path, FileFilter fn) throws IOException {
		ArrayList<File> files = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		createFilesHashRecursively(path, fn, files);
		Collections.sort(files, Comparator.comparing(File::getAbsolutePath));
		for(File fd : files) sb.append(getSha1Checksum(fd));
		return sb.toString();
	}
}
