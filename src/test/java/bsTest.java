import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.doReturn;

public class bsTest {

    @Test
    public void testBs() {
        tbMock obj = Mockito.mock(tbMock.class);
//        doReturn(123L).when(obj).compute();
        Long x = obj.compute();
        Long y = x;
    }
}
