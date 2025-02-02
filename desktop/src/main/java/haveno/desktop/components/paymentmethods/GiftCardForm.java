/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package haveno.desktop.components.paymentmethods;

import com.jfoenix.controls.JFXTextArea;
import haveno.common.util.Tuple2;
import haveno.core.account.witness.AccountAgeWitnessService;
import haveno.core.locale.Country;
import haveno.core.locale.CountryUtil;
import haveno.core.locale.CurrencyUtil;
import haveno.core.locale.TraditionalCurrency;
import haveno.core.locale.Res;
import haveno.core.locale.TradeCurrency;
import haveno.core.offer.Offer;
import haveno.core.payment.CountryBasedPaymentAccount;
import haveno.core.payment.GiftCardAccount;
import haveno.core.payment.PaymentAccount;
import haveno.core.payment.payload.GiftCardAccountPayload;
import haveno.core.payment.payload.PaymentAccountPayload;
import haveno.core.payment.validation.GiftCardValidator;
import haveno.core.util.coin.CoinFormatter;
import haveno.core.util.validation.InputValidator;
import haveno.desktop.components.InputTextField;
import haveno.desktop.util.GUIUtil;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;

import static haveno.desktop.util.FormBuilder.addCompactTopLabelTextArea;
import static haveno.desktop.util.FormBuilder.addCompactTopLabelTextField;
import static haveno.desktop.util.FormBuilder.addCompactTopLabelTextFieldWithCopyIcon;
import static haveno.desktop.util.FormBuilder.addInputTextField;
import static haveno.desktop.util.FormBuilder.addTopLabelTextArea;

public class GiftCardForm extends PaymentMethodForm {
    private final GiftCardAccount GiftCardAccount;
    private final GiftCardValidator GiftCardValidator;
    private Country selectedCountry;

    public static int addFormForBuyer(GridPane gridPane, int gridRow,
                                      PaymentAccountPayload paymentAccountPayload, Offer offer, double top) {
        GiftCardAccountPayload GiftCardAccountPayload = (GiftCardAccountPayload) paymentAccountPayload;
        addCompactTopLabelTextFieldWithCopyIcon(gridPane, ++gridRow, 0, Res.get("shared.country"),
                CountryUtil.getNameAndCode(GiftCardAccountPayload.getCountryCode()), top);
        addCompactTopLabelTextFieldWithCopyIcon(gridPane, gridRow, 1, Res.get("payment.GiftCard.merchant"),
                offer.getGiftCardMerchant(), top);
        TextArea textArea = addTopLabelTextArea(gridPane, gridRow, 1, Res.get("payment.shared.extraInfo"), "").second;
        textArea.setMinHeight(70);
        textArea.setEditable(false);
        textArea.setId("text-area-disabled");
        textArea.setText(offer.getExtraInfo());
        return gridRow;
    }

    public GiftCardForm(PaymentAccount paymentAccount,
                   AccountAgeWitnessService accountAgeWitnessService, GiftCardValidator GiftCardValidator,
                   InputValidator inputValidator, GridPane gridPane, int gridRow, CoinFormatter formatter) {
        super(paymentAccount, accountAgeWitnessService, inputValidator, gridPane, gridRow, formatter);

        this.GiftCardAccount = (GiftCardAccount) paymentAccount;
        this.GiftCardValidator = GiftCardValidator;
    }


    @Override
    public void addFormForAddAccount() {
        gridRowFrom = gridRow + 1;

        Tuple2<ComboBox<TradeCurrency>, Integer> tuple = GUIUtil.addRegionCountryTradeCurrencyComboBoxes(gridPane, gridRow, this::onCountrySelected, this::onTradeCurrencySelected);
        currencyComboBox = tuple.first;
        gridRow = tuple.second;

        InputTextField merchantInputTextField = addInputTextField(gridPane, ++gridRow,
                Res.get("payment.GiftCard.merchant"));
        merchantInputTextField.setPromptText(Res.get("payment.GiftCard.merchant.prompt"));
        merchantInputTextField.setValidator(GiftCardValidator);
        merchantInputTextField.textProperty().addListener((ov, oldValue, newValue) -> {
            GiftCardAccount.setMerchant(newValue);
            updateFromInputs();
        });

        TextArea extraTextArea = addTopLabelTextArea(gridPane, ++gridRow,
                Res.get("payment.shared.optionalExtra"), Res.get("payment.shared.extraInfo.prompt")).second;
        extraTextArea.setMinHeight(70);
        ((JFXTextArea) extraTextArea).setLabelFloat(false);
        //extraTextArea.setValidator(GiftCardValidator);
        extraTextArea.textProperty().addListener((ov, oldValue, newValue) -> {
            GiftCardAccount.setExtraInfo(newValue);
            updateFromInputs();
        });

        addLimitations(false);
        addAccountNameTextFieldWithAutoFillToggleButton();
    }

    private void onCountrySelected(Country country) {
        selectedCountry = country;
        if (selectedCountry != null) {
            getCountryBasedPaymentAccount().setCountry(selectedCountry);
            String countryCode = selectedCountry.code;
            TradeCurrency currency = CurrencyUtil.getCurrencyByCountryCode(countryCode);
            paymentAccount.setSingleTradeCurrency(currency);
            currencyComboBox.setDisable(false);
            currencyComboBox.getSelectionModel().select(currency);

            updateFromInputs();
        }
    }

    private void onTradeCurrencySelected(TradeCurrency tradeCurrency) {
        TraditionalCurrency defaultCurrency = CurrencyUtil.getCurrencyByCountryCode(selectedCountry.code);
        applyTradeCurrency(tradeCurrency, defaultCurrency);
    }

    @Override
    protected void autoFillNameTextField() {
        setAccountNameWithString(GiftCardAccount.getMerchant());
    }

    @Override
    public void addFormForEditAccount() {
        gridRowFrom = gridRow;

        addAccountNameTextFieldWithAutoFillToggleButton();
        addCompactTopLabelTextField(gridPane, ++gridRow, Res.get("shared.paymentMethod"),
                Res.get(paymentAccount.getPaymentMethod().getId()));
        addCompactTopLabelTextField(gridPane, ++gridRow, Res.get("payment.country"),
                getCountryBasedPaymentAccount().getCountry() != null ? getCountryBasedPaymentAccount().getCountry().name : "");
        TradeCurrency singleTradeCurrency = paymentAccount.getSingleTradeCurrency();
        String nameAndCode = singleTradeCurrency != null ? singleTradeCurrency.getNameAndCode() : "null";
        addCompactTopLabelTextField(gridPane, ++gridRow, Res.get("shared.currency"), nameAndCode);
        addCompactTopLabelTextField(gridPane, ++gridRow, Res.get("payment.GiftCard.merchant", GiftCardAccount.getMerchant()),
                GiftCardAccount.getMerchant());
        TextArea textArea = addCompactTopLabelTextArea(gridPane, ++gridRow, Res.get("payment.shared.extraInfo"), "").second;
        textArea.setText(GiftCardAccount.getExtraInfo());
        textArea.setMinHeight(70);
        textArea.setEditable(false);

        addLimitations(true);
    }

    @Override
    public void updateAllInputsValid() {
        allInputsValid.set(isAccountNameValid()
                && GiftCardValidator.validate(GiftCardAccount.getMerchant()).isValid
                && GiftCardAccount.getTradeCurrencies().size() > 0);
    }

    private CountryBasedPaymentAccount getCountryBasedPaymentAccount() {
        return (CountryBasedPaymentAccount) this.paymentAccount;
    }
}
