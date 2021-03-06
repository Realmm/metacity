package org.metacity.metacity.util;

import de.tr7zw.changeme.nbtapi.NBTItem;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.metacity.metacity.token.TokenModel;

import javax.annotation.Nonnull;
import java.math.BigInteger;

public class TokenUtils {

    public static final String BASE_INDEX   = "0000000000000000";
    public static final int    INDEX_LENGTH = BASE_INDEX.length();
    public static final int    ID_LENGTH    = 16;

    private TokenUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean isNonFungible(ItemStack is) {
        if (is != null && is.getType() != Material.AIR) {
            NBTItem nbtItem = new NBTItem(is);

            if (nbtItem.hasKey(TokenModel.NBT_NONFUNGIBLE))
                return nbtItem.getBoolean(TokenModel.NBT_NONFUNGIBLE);
        }

        return false;
    }

    public static String getTokenID(String fullId) throws IllegalArgumentException {
        if (!isValidFullId(fullId))
            throw new IllegalArgumentException("Invalid full ID");

        return fullId.substring(0, ID_LENGTH);
    }

    public static String getTokenID(ItemStack is) {
        if (is != null && is.getType() != Material.AIR) {
            NBTItem nbtItem = new NBTItem(is);

            if (nbtItem.hasKey(TokenModel.NBT_ID))
                return nbtItem.getString(TokenModel.NBT_ID);

            return "";
        }

        return null;
    }

    public static String getTokenIndex(String fullId) throws IllegalArgumentException {
        if (!isValidFullId(fullId))
            throw new IllegalArgumentException("Invalid full ID");

        return fullId.substring(ID_LENGTH);
    }

    public static String getTokenIndex(ItemStack is) {
        if (is != null && is.getType() != Material.AIR) {
            NBTItem nbtItem = new NBTItem(is);
            if (nbtItem.hasKey(TokenModel.NBT_INDEX))
                return nbtItem.getString(TokenModel.NBT_INDEX);

            return "";
        }

        return null;
    }

    public static boolean hasTokenData(ItemStack is) {
        if (is == null || is.getType() == Material.AIR)
            return false;

        NBTItem nbtItem = new NBTItem(is);
        return nbtItem.hasKey(TokenModel.NBT_ID)
                || nbtItem.hasKey(TokenModel.NBT_INDEX)
                || nbtItem.hasKey(TokenModel.NBT_NONFUNGIBLE);
    }

    public static boolean isValidTokenItem(ItemStack is) {
        if (is == null || is.getType() == Material.AIR)
            return false;

        NBTItem nbtItem     = new NBTItem(is);
        String  id          = nbtItem.getString(TokenModel.NBT_ID);
        String  index       = nbtItem.getString(TokenModel.NBT_INDEX);
        Boolean nonfungible = nbtItem.getBoolean(TokenModel.NBT_NONFUNGIBLE);
        return isValidId(id)
                && isValidIndex(index)
                && nonfungible != null
                && (nonfungible ^ index.equals(BASE_INDEX));
    }

    public static boolean isValidFullId(String fullId) {
        return isValidFullId(fullId, false);
    }

    public static boolean isValidFullId(String fullId, boolean ignoreCase) {
        return isValidString(fullId, ID_LENGTH + INDEX_LENGTH, ignoreCase);
    }

    public static boolean isValidId(String id) {
        return isValidId(id, false);
    }

    public static boolean isValidId(String id, boolean ignoreCase) {
        return isValidString(id, ID_LENGTH, ignoreCase);
    }

    public static boolean isValidIndex(String index) {
        return isValidIndex(index, false);
    }

    public static String bigIntToIndex(BigInteger value) {
        if (value == null)
            throw new IllegalStateException("value cannot be null");

        StringBuilder hex = new StringBuilder(value.toString(16));

        if (hex.length() > BASE_INDEX.length())
            throw new IllegalStateException("value is too big");

        while (hex.length() < BASE_INDEX.length()) {
            hex.insert(0, "0");
        }

        return hex.toString();
    }

    public static boolean isValidIndex(String index, boolean ignoreCase) {
        return isValidString(index, INDEX_LENGTH, ignoreCase);
    }

    private static boolean isValidString(String s, int length, boolean ignoreCase) {
        if (s == null || s.length() != length)
            return false;

        for (char ch : s.toCharArray()) {
            if (!isValidCharacter(ch, ignoreCase))
                return false;
        }

        return true;
    }

    private static boolean isValidCharacter(char ch, boolean ignoreCase) {
        return (ch >= '0' && ch <= '9')
                || (ch >= 'a' && ch <= 'f')
                || (ignoreCase && (ch >= 'A' && ch <= 'F'));
    }

    public static String createFullId(@NotNull TokenModel tokenModel) throws NullPointerException {
        return createFullId(tokenModel.getId(), tokenModel.getIndex());
    }

    public static String createFullId(@NotNull String id) throws IllegalArgumentException, NullPointerException {
        return createFullId(id, TokenUtils.BASE_INDEX);
    }

    public static String createFullId(@NotNull String id,
                                      String index) throws IllegalArgumentException, NullPointerException {
        id    = formatId(id);
        index = index == null
                ? BASE_INDEX
                : formatIndex(index);
        return id + index;
    }

    public static String formatId(String id) throws IllegalArgumentException {
        if (id.length() != ID_LENGTH)
            throw new IllegalArgumentException("Provided string is not the ID length");

        char[] chars = new char[ID_LENGTH];
        for (int i = 0; i < ID_LENGTH; i++) {
            chars[i] = formatCharacter(id.charAt(i));
        }

        return new String(chars);
    }

    public static String formatIndex(String index) throws IllegalArgumentException {
        if (index.length() > INDEX_LENGTH)
            throw new IllegalArgumentException("Provided string is larger than the index length");

        char[] chars = new char[INDEX_LENGTH];
        for (int i = 0; i < INDEX_LENGTH; i++) {
            char ch = i < index.length()
                    ? formatCharacter(index.charAt(index.length() - i - 1))
                    : '0';
            chars[INDEX_LENGTH - i - 1] = ch;
        }

        return new String(chars);
    }

    private static char formatCharacter(char ch) throws IllegalArgumentException {
        if (ch >= 'A' && ch <= 'F')
            ch += 32; // To lowercase

        if (!isValidCharacter(ch, false))
            throw new IllegalArgumentException("Provided string is not in hexadecimal");

        return ch;
    }

    public static String trimIndex(String index) throws IllegalArgumentException {
        if (!isValidIndex(index))
            throw new IllegalArgumentException("Provided string is not a valid index");

        for (int i = 0; i < index.length(); i++) {
            if (index.charAt(i) != '0')
                return index.substring(i);
        }

        return "";
    }

    public static String toFullId(String id) {
        if (isValidFullId(id))
            return id;

        return createFullId(id);
    }

    public static String normalizeFullId(String fullId) throws IllegalArgumentException {
        if (!isValidFullId(fullId))
            throw new IllegalArgumentException("Provided string is not a valid full id");

        return createFullId(getTokenID(fullId));
    }

    public static Long convertIndexToLong(String index) throws IllegalArgumentException {
        if (!isValidIndex(index))
            throw new IllegalArgumentException("Provided string is not a valid index");

        return Long.parseLong(index, 16);
    }

    public static boolean canCombineStacks(ItemStack first, ItemStack second) {
        String firstId   = getTokenID(first);
        String secondId  = getTokenID(second);
        boolean firstNF  = isNonFungible(first);
        boolean secondNF = isNonFungible(second);

        if (firstId.isEmpty()
                || secondId.isEmpty()
                || firstNF
                || secondNF
                || !firstId.equals(secondId))
            return false;

        int maxStackSize = first.getMaxStackSize();
        return maxStackSize == second.getMaxStackSize()
                && first.getType() == second.getType()
                && first.getAmount() + second.getAmount() <= maxStackSize;
    }

    public static String parseIndex(@NonNull String index) throws IllegalArgumentException, NullPointerException {
        boolean hexString = false;
        if (index.startsWith("x") || index.startsWith("X")) {
            hexString = true;
            index = index.substring(1);
        } else if (index.startsWith("#")) {
            index = index.substring(1);
        }

        if (index.isEmpty()) {
            throw new IllegalArgumentException("Index may not be empty");
        } else if (!hexString) {
            long parsedLong = Long.parseLong(index);
            if (parsedLong < 1L)
                throw new IllegalArgumentException("Provided index is not positive");

            index = Long.toHexString(parsedLong);
        }

        index = TokenUtils.formatIndex(index);
        if (index.equals(TokenUtils.BASE_INDEX))
            throw new IllegalArgumentException("Index may not be the base index");

        return index;
    }

}
