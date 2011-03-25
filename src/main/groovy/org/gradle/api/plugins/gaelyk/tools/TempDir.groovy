package org.gradle.api.plugins.gaelyk.tools

class TempDir {
	
	@Lazy static tempDir = new File(System.getProperty('java.io.tmpdir'))
	
	static File createNew(prefix, suffix = new Random().nextInt()){
		File where = findDir(prefix, suffix)
		if(!where.exists()) {
			where.mkdir()
		}
		where
	}
	
	static File findDir(prefix, suffix){
		new File(tempDir, "$prefix-$suffix")
	}
	
	static withTempDir(prefix, suffix = new Random().nextInt(), Closure closure){
		File where = findDir(prefix, suffix)
		try{
			closure.call(where)
		} finally {
			new AntBuilder().delete dir: where.path
		}
	}
	
}
