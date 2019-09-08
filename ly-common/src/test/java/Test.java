import com.leyou.common.enuums.ExceptionEnum;
import com.leyou.common.exception.LyException;

public class Test {

    public static void main(String[] args) {
        throw new LyException(ExceptionEnum.PRICE_CANNOT_BE_NULL);
    }

}
