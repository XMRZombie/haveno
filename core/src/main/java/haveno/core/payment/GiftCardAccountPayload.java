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

package haveno.core.payment.payload;

import com.google.protobuf.Message;
import haveno.core.locale.Res;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@ToString
@Setter
@Getter
@Slf4j
public final class GiftCardAccountPayload extends CountryBasedPaymentAccountPayload {
    private String merchant = "";
    private String extraInfo = "";

    public GiftCardAccountPayload(String paymentMethod, String id) {
        super(paymentMethod, id);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // PROTO BUFFER
    ///////////////////////////////////////////////////////////////////////////////////////////

    private GiftCardAccountPayload(String paymentMethodName,
                              String id,
                              String countryCode,
                              List<String> acceptedCountryCodes,
                              String merchant,
                              String extraInfo,
                              long maxTradePeriod,
                              Map<String, String> excludeFromJsonDataMap) {
        super(paymentMethodName,
                id,
                countryCode,
                acceptedCountryCodes,
                maxTradePeriod,
                excludeFromJsonDataMap);
        this.merchant = merchant;
        this.extraInfo = extraInfo;
    }

    @Override
    public Message toProtoMessage() {
        protobuf.GiftCardAccountPayload.Builder builder = protobuf.GiftCardAccountPayload.newBuilder()
                .setMerchant(merchant)
                .setExtraInfo(extraInfo);
        final protobuf.CountryBasedPaymentAccountPayload.Builder countryBasedPaymentAccountPayload = getPaymentAccountPayloadBuilder()
                .getCountryBasedPaymentAccountPayloadBuilder()
                .setGiftCardAccountPayload(builder);
        return getPaymentAccountPayloadBuilder()
                .setCountryBasedPaymentAccountPayload(countryBasedPaymentAccountPayload)
                .build();
    }

    public static PaymentAccountPayload fromProto(protobuf.PaymentAccountPayload proto) {
        protobuf.CountryBasedPaymentAccountPayload countryBasedPaymentAccountPayload = proto.getCountryBasedPaymentAccountPayload();
        protobuf.GiftCardAccountPayload GiftCardAccountPayloadPB = countryBasedPaymentAccountPayload.getGiftCardAccountPayload();
        return new GiftCardAccountPayload(proto.getPaymentMethodId(),
                proto.getId(),
                countryBasedPaymentAccountPayload.getCountryCode(),
                new ArrayList<>(countryBasedPaymentAccountPayload.getAcceptedCountryCodesList()),
                GiftCardAccountPayloadPB.getMerchant(),
                GiftCardAccountPayloadPB.getExtraInfo(),
                proto.getMaxTradePeriod(),
                new HashMap<>(proto.getExcludeFromJsonDataMap()));
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // API
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public String getPaymentDetails() {
                Res.getWithCol("payment.GiftCard.merchant") + " " + merchant +
                ", " + Res.getWithCol("payment.shared.extraInfo") + " " + extraInfo;
    }


    @Override
    public String getPaymentDetailsForTradePopup() {
                Res.getWithCol("payment.GiftCard.merchant") + " " + merchant +
                ", " + Res.getWithCol("payment.shared.extraInfo") + " " + extraInfo;
    }

    @Override
    public byte[] getAgeWitnessInputData() {
        // Using the merchant and extrainfo instead of contact and city (from F2F template)
        return super.getAgeWitnessInputData(ArrayUtils.addAll(extraInfo.getBytes(StandardCharsets.UTF_8),
                merchant.getBytes(StandardCharsets.UTF_8)));
    }
}
