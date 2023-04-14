public class DisplayUtil {

    public static String pad(String str, int pad) {
		int length = str.length();
		if (length >= pad)
			return str;
		StringBuilder builder = new StringBuilder();
		builder.append(str);
		for (int i = 0; i < pad - length; i++)
			builder.append(" ");
		return builder.toString();
	}

}
