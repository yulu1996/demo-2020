import java.math.BigDecimal;

public class cal {
    public static void main(String[] args) {
        int i1 = 10, i2 = 10;
        System.out.println("i1+i2=" + i1 + i2);
        System.out.println("i1-i2=" + (i1 - i2));
        System.out.println("i1*i2=" + i1 * i2);
        System.out.println("i1/i2=" + i1 / i2);

        BigDecimal a = new BigDecimal(11);
        BigDecimal b = new BigDecimal("10.9");
        System.out.println(a.subtract(b));
    }
}
