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

package haveno.common.proto;

import com.google.common.base.Enums;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import com.google.protobuf.ProtocolStringList;
import haveno.common.Proto;
import haveno.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class ProtoUtil {

    public static Set<byte[]> byteSetFromProtoByteStringList(List<ByteString> byteStringList) {
        return byteStringList.stream()
                .map(ByteString::toByteArray)
                .collect(Collectors.toCollection(CopyOnWriteArraySet::new));
    }

    /**
     * Returns the input String, except when it's the empty string: "", then null is returned.
     * Note: "" is the default value for a protobuffer string, so this means it's not filled in.
     */
    @Nullable
    public static String stringOrNullFromProto(String proto) {
        return "".equals(proto) ? null : proto;
    }

    @Nullable
    public static byte[] byteArrayOrNullFromProto(ByteString proto) {
        return proto.isEmpty() ? null : proto.toByteArray();
    }

    /**
     * Get a Java enum from a Protobuf enum in a safe way.
     *
     * @param enumType the class of the enum, e.g: BlaEnum.class
     * @param name     the name of the enum entry, e.g: proto.getWinner().name()
     * @param <E>      the enum Type
     * @return an enum
     */
    @Nullable
    public static <E extends Enum<E>> E enumFromProto(Class<E> enumType, String name) {
        String enumName = name != null ? name : "UNDEFINED";
        AtomicReference<E> result = new AtomicReference<>(Enums.getIfPresent(enumType, enumName).orNull());
        if (result.get() == null) {
            result.set(Enums.getIfPresent(enumType, "UNDEFINED").orNull());
            log.debug("We try to lookup for an enum entry with name 'UNDEFINED' and use that if available, " +
                    "otherwise the enum is null. enum={}", result.get());
        }
        return result.get();
    }

    public static <T extends Message> Iterable<T> collectionToProto(Collection<? extends Proto> collection,
                                                                    Class<T> messageType) {
        return collection.stream()
                .map(e -> {
                    final Message message = e.toProtoMessage();
                    try {
                        return messageType.cast(message);
                    } catch (ClassCastException t) {
                        log.error("Message could not be cast. message={}, messageType={}", message, messageType);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
    }

    public static <T> Iterable<T> collectionToProto(Collection<? extends Proto> collection,
                                                    Function<? super Message, T> extra) {
        return collection.stream()
                .map(o -> extra.apply(o.toProtoMessage()))
                .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
    }

    public static List<String> protocolStringListToList(ProtocolStringList protocolStringList) {
        return CollectionUtils.isEmpty(protocolStringList) ? new CopyOnWriteArrayList<>() : new CopyOnWriteArrayList<>(protocolStringList);
    }

    public static Set<String> protocolStringListToSet(ProtocolStringList protocolStringList) {
        return CollectionUtils.isEmpty(protocolStringList) ? new CopyOnWriteArraySet<>() : new CopyOnWriteArraySet<>(protocolStringList);
    }
}
