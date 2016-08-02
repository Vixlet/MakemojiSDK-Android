package com.makemoji.mojilib;

/**
 * https://gist.github.com/jdcrensh/4670128
 * Created by Scott Baar on 3/20/2016.
 */
public class Base62 {

        private String characters;

        /**
         * Constructs a Base62 object with the default charset (0..9a..zA..Z).
         */
        public Base62() {
            this("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
        }

        /**
         * Constructs a Base62 object with a custom charset.
         *
         * @param characters
         *            the charset to use. Must be 62 characters.
         * @throws <code>IllegalArgumentException<code> if the supplied charset is not 62 characters long.
         */
        public Base62(String characters) {
            if (!(characters.length() == 62)) {
                throw new IllegalArgumentException("Invalid string length, must be 62.");
            }
            this.characters = characters;
        }

        /**
         * Encodes a decimal value to a Base62 <code>String</code>.
         *
         * @param b10
         *            the decimal value to encode, must be nonnegative.
         * @return the number encoded as a Base62 <code>String</code>.
         */
        public String encodeBase10(long b10) {
            if (b10 < 0) {
                throw new IllegalArgumentException("b10 must be nonnegative");
            }
            String ret = "";
            while (b10 > 0) {
                ret = characters.charAt((int) (b10 % 62)) + ret;
                b10 /= 62;
            }
            return ret;

        }

        /**
         * Decodes a Base62 <code>String</code> returning a <code>long</code>.
         *
         * @param b62
         *            the Base62 <code>String</code> to decode.
         * @return the decoded number as a <code>long</code>.
         * @throws IllegalArgumentException
         *             if the given <code>String</code> contains characters not
         *             specified in the constructor.
         */
        public long decodeBase62(String b62) {
            for (char character : b62.toCharArray()) {
                if (!characters.contains(String.valueOf(character))) {
                    throw new IllegalArgumentException("Invalid character(s) in string: " + character);
                }
            }
            long ret = 0;
            b62 = new StringBuffer(b62).reverse().toString();
            long count = 1;
            for (char character : b62.toCharArray()) {
                ret += characters.indexOf(character) * count;
                count *= 62;
            }
            return ret;
        }
}