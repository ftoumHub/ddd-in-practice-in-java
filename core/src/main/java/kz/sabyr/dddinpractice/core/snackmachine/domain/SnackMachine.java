package kz.sabyr.dddinpractice.core.snackmachine.domain;

import kz.sabyr.dddinpractice.common.AggregateRoot;
import kz.sabyr.dddinpractice.core.common.domain.Money;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static java.math.BigDecimal.ZERO;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;
import static kz.sabyr.dddinpractice.core.common.domain.Money.*;

@Getter
public class SnackMachine extends AggregateRoot {

    private Money moneyInside;
    private BigDecimal moneyInTransaction;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private List<Slot> slots;

    public SnackMachine() {
        moneyInside = NONE;
        moneyInTransaction = ZERO;

        slots = asList(
                new Slot(this, 1),
                new Slot(this, 2),
                new Slot(this, 3)
        );
    }

    public void loadMoney(Money money) {
        moneyInside = Money.add(moneyInside, money);
    }

    public void insertMoney(Money money) {
        List<Money> allowedCoins = asList(ONE_CENT, TEN_CENT, QUARTER_DOLLAR, ONE_DOLLAR, FIVE_DOLLAR, TWENTY_DOLLAR);
        if (!allowedCoins.contains(money)) {
            throw new IllegalArgumentException();
        }
        moneyInTransaction = moneyInTransaction.add(money.getAmount());
        moneyInside = Money.add(moneyInside, money);
    }

    public void returnMoney() {
        Money money = moneyInside.allocate(moneyInTransaction);
        moneyInTransaction = ZERO;
        moneyInside = Money.subtract(moneyInside, money);
    }

    public void buySnack(int position) {
        String canBuyMessage = canBuySnack(position);

        if (canBuyMessage.equals("The snack pile is empty")) {
            throw new IllegalArgumentException(canBuyMessage);
        }

        if (canBuyMessage.equals("Not enough money")) {
            throw new IllegalStateException(canBuyMessage);
        }

        Slot slot = slots.stream().filter(s -> s.getPosition() == position).findFirst().orElse(null);
        Money allocated = moneyInside.allocate(moneyInTransaction.subtract(slot.getSnackPile().getPrice()));
        slot.setSnackPile(slot.getSnackPile().subtractOne());
        moneyInside = Money.subtract(moneyInside, allocated);
        moneyInTransaction = ZERO;
    }

    public void loadSnacks(int position, SnackPile snackPile) {
        Slot slot = slots.stream().filter(s -> s.getPosition() == position).findFirst().orElse(null);
        slot.setSnackPile(snackPile);
    }

    public String canBuySnack(int position) {
        SnackPile snackPile = getSnackPile(position);
        if (snackPile.getQuantity() <= 0) {
            return "The snack pile is empty";
        }

        if (moneyInTransaction.compareTo(snackPile.getPrice()) < 0) {
            return "Not enough money";
        }

        if (!moneyInside.canAllocateMoney(moneyInTransaction.subtract(snackPile.getPrice()))) {
            return "Not enough change";
        }

        return "";
    }

    public SnackPile getSnackPile(int position) {
        return slots.stream().filter(s -> s.getPosition() == position).findFirst().orElse(null).getSnackPile();
    }

    public List<SnackPile> getAllSnackPiles() {
        List<SnackPile> piles = slots
                .stream()
                .map(Slot::getSnackPile).collect(toList());
        piles.sort(Comparator.comparingInt(SnackPile::getQuantity));

        return unmodifiableList(piles);
    }

    public static class SnackMachineBuilder {
        private long id;
        private List<Slot> slots;

        public SnackMachineBuilder(long id) {
            this.id = id;
        }

        public SnackMachineBuilder withSlots(List<Slot> slots) {
            this.slots = slots;
            return this;
        }

        public SnackMachine build() {
            SnackMachine snackMachine = new SnackMachine();
            snackMachine.setId(id);
            snackMachine.slots = slots;
            return snackMachine;
        }
    }
}
