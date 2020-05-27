import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class CalculateTest {

    BigDecimal bignum1 = new BigDecimal("6");
    BigDecimal bignum2 = new BigDecimal("3");

    @Test
    public void testAdd(){
        assertEquals(new BigDecimal("9"),new Calculate().add(bignum1, bignum2));
    }

    @Test
    public void testSubstract(){
        assertEquals(new BigDecimal("3"),new Calculate().subtract(bignum1, bignum2));
    }

    @Test
    public void testMultiply(){
        assertEquals(new BigDecimal("18"),new Calculate().multiply(bignum1, bignum2));
    }

    @Test
    public void testDivide(){
        assertEquals(new BigDecimal("2"),new Calculate().divide(bignum1, bignum2));
    }



    String str1 = new String ("abc");
    String str2 = new String ("abc");
    String str3 = null;
    String str4 = "abc";
    String str5 = "abc";
    int val1 = 5;
    int val2 = 6;
    String[] expectedArray = {"one", "two", "three"};
    String[] resultArray =  {"one", "two", "three"};


    @Test
    public void testAssert(){
        //检查两个变量或者等式是否平等
        assertEquals(str1, str2);

        //检查条件为真
        assertTrue (val1 < val2);

        //检查条件为假
        assertFalse(val1 > val2);

        //检查对象不为空
        assertNotNull(str1);

        //检查对象为空
        assertNull(str3);

        //检查两个相关对象是否指向同一个对象
        assertSame(str4,str5);

        //检查两个相关对象是否不指向同一个对象
        assertNotSame(str1,str3);

        //检查两个数组是否相等
        assertArrayEquals(expectedArray, resultArray);
    }


}
