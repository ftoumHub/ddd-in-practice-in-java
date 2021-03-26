package kz.sabyr.dddinpractice.core.snackmachine.domain;

import kz.sabyr.dddinpractice.core.common.domain.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static kz.sabyr.dddinpractice.core.common.domain.Money.*;
import static org.assertj.core.api.Assertions.*;

public class SnackMachineTest {

    @Test
    void return_Money_Empties_Money_In_Transaction() {
        SnackMachine snackMachine = new SnackMachine();
        snackMachine.insertMoney(ONE_DOLLAR);

        snackMachine.returnMoney();

        assertThat(snackMachine.getMoneyInTransaction())
                .isEqualByComparingTo(ZERO);
    }

    @Test
    void insert_Money_Goes_To_Money_In_Transaction() {
        SnackMachine snackMachine = new SnackMachine();
        snackMachine.insertMoney(ONE_DOLLAR);

        assertThat(snackMachine.getMoneyInTransaction()).isEqualByComparingTo(ONE);
        assertThat(snackMachine.getMoneyInside().getAmount()).isEqualByComparingTo(ONE);
    }

    @Test
    void cannot_Insert_More_Than_One_Coin_Or_Note_At_A_Time() {
        SnackMachine snackMachine = new SnackMachine();

        assertThatThrownBy(() -> snackMachine.insertMoney(Money.add(ONE_CENT, ONE_CENT)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void buy_Snack_Trades_Inserted_Money_For_A_Snack() {
        SnackMachine snackMachine = new SnackMachine();
        snackMachine.loadSnacks(1, new SnackPile(Snack.CHOCOLATE, 10, new BigDecimal("1")));
        snackMachine.insertMoney(ONE_DOLLAR);

        snackMachine.buySnack(1);

        assertThat(snackMachine.getMoneyInside()).isEqualTo(ONE_DOLLAR);
        assertThat(snackMachine.getMoneyInTransaction()).isEqualByComparingTo(ZERO);
        assertThat(snackMachine.getSnackPile(1).getQuantity()).isEqualTo(9);
    }

    @Test
    void cannot_Buy_Snack_When_There_Is_No_Snack() {
        SnackMachine snackMachine = new SnackMachine();
        assertThatThrownBy(() -> snackMachine.buySnack(1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cannot_Make_Purchase_When_There_Is_Not_Enough_Money() {
        SnackMachine snackMachine = new SnackMachine();
        snackMachine.loadSnacks(1, new SnackPile(Snack.CHOCOLATE, 1, new BigDecimal("2")));
        snackMachine.insertMoney(ONE_DOLLAR);
        assertThatThrownBy(() -> snackMachine.buySnack(1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void machine_Should_Return_Money_With_Highest_Denomination_First() {
        SnackMachine snackMachine = new SnackMachine();
        snackMachine.loadMoney(ONE_DOLLAR);

        snackMachine.insertMoney(QUARTER_DOLLAR);
        snackMachine.insertMoney(QUARTER_DOLLAR);
        snackMachine.insertMoney(QUARTER_DOLLAR);
        snackMachine.insertMoney(QUARTER_DOLLAR);

        snackMachine.returnMoney();

        assertThat(snackMachine.getMoneyInside().getQuarterCount()).isEqualTo(4);
        assertThat(snackMachine.getMoneyInside().getOneDollarCount()).isEqualTo(0);
    }

    @Test
    void after_Purchase_Change_Is_Returned() {
        SnackMachine snackMachine = new SnackMachine();
        snackMachine.loadSnacks(1, new SnackPile(Snack.CHOCOLATE, 1, new BigDecimal("0.5")));
        snackMachine.loadMoney(Money.multiplyByMultiplier(Money.TEN_CENT, 10));

        snackMachine.insertMoney(ONE_DOLLAR);

        snackMachine.buySnack(1);

        assertThat(snackMachine.getMoneyInside().getAmount()).isEqualByComparingTo(new BigDecimal("1.5"));
        assertThat(snackMachine.getMoneyInTransaction()).isEqualByComparingTo(ZERO);
    }

    @Test
    void cannot_Buy_Snack_If_Not_Enough_Change() {
        SnackMachine snackMachine = new SnackMachine();
        snackMachine.loadSnacks(1, new SnackPile(Snack.CHOCOLATE, 1, new BigDecimal("0.5")));
        snackMachine.loadMoney(Money.multiplyByMultiplier(ONE_DOLLAR, 1));

        snackMachine.insertMoney(ONE_DOLLAR);

        assertThatThrownBy(() -> snackMachine.buySnack(1))
                .isInstanceOf(IllegalStateException.class);
    }
}
