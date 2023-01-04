import java.io.IOException;

public class Main {
	public static void main(String[] args) {
		try {
			new Map("42.png").save("2.png");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
