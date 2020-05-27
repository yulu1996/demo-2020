import org.junit.*;

public class test {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        System.out.println("this is beforeClass....");
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        System.out.println("this is afterClass....");
    }

    @Before
    public void setUp() throws Exception {
        System.out.println("this is before....");
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("this is after....");
    }

    @Test
    public void test1() {
        System.out.println("this is test1....");
    }

    @Test
    public void test2() {
        System.out.println("this is test2....");
    }

}
