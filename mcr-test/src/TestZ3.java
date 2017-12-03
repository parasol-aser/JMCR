import java.io.IOException;

public class TestZ3 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		 ProcessBuilder processBuilder = new ProcessBuilder("/usr/local/bin/z3");
		 try {
			processBuilder.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
