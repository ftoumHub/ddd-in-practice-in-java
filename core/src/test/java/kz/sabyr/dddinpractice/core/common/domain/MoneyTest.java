package kz.sabyr.dddinpractice.core.common.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


public class MoneyTest {

    @Test
    void sum_Of_Two_Moneys_Produces_Correct_Result() {
        Money money1 = new Money(1,2,3,4,5,6);
        Money money2 = new Money(1,2,3,4,5,6);
        Money sum = Money.add(money1, money2);

        assertThat(sum.getOneCentCount()).isEqualTo(2);
        assertThat(sum.getTenCentCount()).isEqualTo(4);
        assertThat(sum.getQuarterCount()).isEqualTo(6);
        assertThat(sum.getOneDollarCount()).isEqualTo(8);
        assertThat(sum.getFiveDollarCount()).isEqualTo(10);
        assertThat(sum.getTwentyDollarCount()).isEqualTo(12);
    }

    @Test
    void two_Money_Instances_Equal_If_Contain_The_Same_Money_Amount() {
        Money money1 = new Money(1,2,3,4,5,6);
        Money money2 = new Money(1,2,3,4,5,6);

        assertThat(money1).isEqualTo(money2);
    }

    @Test
    void two_Money_Instances_Do_Not_Equal_If_Contain_Diff_Money_Values() {
        Money money1 = new Money(1,0,0,0,0,0);
        Money money2 = new Money(0,0,0,1,0,0);

        assertThat(money1).isNotEqualTo(money2);
    }

    @ParameterizedTest
    @CsvSource({
            "-1, 0, 0, 0, 0, 0",
            "0, -2, 0, 0, 0, 0",
            "0, 0, -3, 0, 0, 0",
            "0, 0, 0, -4, 0, 0",
            "0, 0, 0, 0, -5, 0",
            "0, 0, 0, 0, 0, -6",
    })
    void cannot_Create_Money_With_Negative_Value(
        int oneCent,
        int tenCent,
        int quarterCent,
        int oneDollar,
        int fiveDollar,
        int twentyDollar) {
        assertThatThrownBy(
                () -> new Money(oneCent, tenCent, quarterCent, oneDollar, fiveDollar, twentyDollar)
        )
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @CsvSource({
            "0,0,0,0,0,0,0",
            "1,0,0,0,0,0,0.01",
            "1,2,0,0,0,0,0.21",
            "1,2,3,0,0,0,0.96",
            "1,2,3,4,0,0,4.96",
            "1,2,3,4,5,0,29.96",
            "1,2,3,4,5,6,149.96",
            "11,0,0,0,0,0,0.11",
            "110,0,0,0,100,0,501.1"
    })
    void amount_Is_Calculated_Correctly(
            int oneCent,
            int tenCent,
            int quarterCent,
            int oneDollar,
            int fiveDollar,
            int twentyDollar,
            BigDecimal expectedAmount) {
        Money money = new Money(oneCent, tenCent, quarterCent, oneDollar, fiveDollar, twentyDollar);
        assertThat(money.getAmount()).isEqualByComparingTo(expectedAmount);
    }

    @ParameterizedTest
    @CsvSource({
            "0,0,0,0,0,0, 0.00 cents",
            "1,0,0,0,0,0, 1.00 cents",
            "1,2,0,0,0,0, 21.00 cents",
            "1,2,3,4,0,0, 4.96 dollars"
    })
    void to_String_Should_Return_Amount_Of_Money(
            int oneCent,
            int tenCent,
            int quarterCent,
            int oneDollar,
            int fiveDollar,
            int twentyDollar,
            String expectedAmount) {
        Money money = new Money(oneCent, tenCent, quarterCent, oneDollar, fiveDollar, twentyDollar);
        assertThat(money.toString()).isEqualTo(expectedAmount);
    }

    @Test
    void subtraction_Of_Two_Moneys_Produce_Correct_Result() {
        Money money1 = new Money(10, 10, 10, 10, 10, 10);
        Money money2 = new Money(1,2,3,4,5,6);

        Money result = Money.subtract(money1, money2);

        assertThat(result.getOneCentCount()).isEqualTo(9);
        assertThat(result.getTenCentCount()).isEqualTo(8);
        assertThat(result.getQuarterCount()).isEqualTo(7);
        assertThat(result.getOneDollarCount()).isEqualTo(6);
        assertThat(result.getFiveDollarCount()).isEqualTo(5);
        assertThat(result.getTwentyDollarCount()).isEqualTo(4);
    }

    @Test
    void cannot_Subtract_More_Than_Exists() {
        Money money1 = new Money(1,0,0,0,0,0);
        Money money2 = new Money(0,1,0,0,0,0);

        assertThatThrownBy(
                () -> Money.subtract(money1, money2)
        )
                .isInstanceOf(IllegalArgumentException.class);
    }
}