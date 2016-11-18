package nablarch.core.validation.ee;

import java.util.Locale;

import javax.validation.ConstraintViolation;
import javax.validation.Path;

import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.core.message.Message;
import nablarch.core.message.MessageNotFoundException;
import nablarch.core.message.MessageUtil;
import nablarch.core.message.StringResource;
import nablarch.core.util.StringUtil;
import nablarch.core.validation.ValidationResultMessage;

/**
 * 項目名付きのメッセージ変換を行うクラスを生成するファクトリクラス。
 *
 * @author Hisaaki Shioiri
 */
public class ItemNamedConstraintViolationConverterFactory extends ConstraintViolationConverterFactory {

    @Override
    public ConstraintViolationConverter create() {
        return new ItemNamedConstraintViolationConverter();
    }

    @Override
    public ConstraintViolationConverter create(final String prefix) {
        return new ItemNamedConstraintViolationConverter(prefix);
    }

    /**
     * 項目名付きのメッセージ変換を行うクラス。
     */
    private static class ItemNamedConstraintViolationConverter extends ConstraintViolationConverter {

        /** プロパティ名の先頭に付加するプレフィックス */
        private final String prefix;

        /**
         * プレフィックスなしで生成する。
         */
        public ItemNamedConstraintViolationConverter() {
            prefix = "";
        }

        /**
         * プレフィックス付きで生成する。
         *
         * @param prefix プレフィックス
         */
        public ItemNamedConstraintViolationConverter(final String prefix) {
            if (StringUtil.hasValue(prefix)) {
                this.prefix = prefix + '.';
            } else {
                this.prefix = "";
            }
        }

        @Override
        public Message convert(final ConstraintViolation<?> violation) {
            final Object bean = violation.getLeafBean();

            final String messageId;
            if (bean != null) {
                messageId = createItemNameMessageId(bean.getClass(), getLeafPropertyName(violation));
            } else {
                messageId = "";
            }

            final StringResource stringResource = new ViolationBasedStringResourceWithItemName(messageId, violation);
            return new ValidationResultMessage(prefix + violation.getPropertyPath(), stringResource, null);
        }

        /**
         * 項目名を表すメッセージIDを生成する。
         *
         * @param clazz バリデーション対象のクラス
         * @param propertyName プロパティ名
         * @return 項目名を示すメッセージID
         */
        private static String createItemNameMessageId(final Class<?> clazz, final String propertyName) {
            if (StringUtil.isNullOrEmpty(propertyName)) {
                return null;
            }
            final String className = clazz.getName()
                                          .replace('$', '.');
            return className + '.' + propertyName;
        }

        /**
         * 末端のプロパティ名を取得する。
         *
         * @param violation {@code ConstraintViolation}
         * @return 末端のプロパティ名
         */
        private static String getLeafPropertyName(final ConstraintViolation<?> violation) {
            final Path path = violation.getPropertyPath();
            String propertyName = null;
            for (final Path.Node node : path) {
                propertyName = node.getName();
            }
            return propertyName;
        }
    }

    /**
     * 項目名付きのメッセージを持つ{@link StringResource}実装クラス。
     * <p>
     * 項目名のメッセージIDに対応するメッセージ(項目名)が存在している場合には、
     * バリデーション結果のメッセージの先頭に項目名を付加する。
     * <p>
     * 例えば、バリデーション結果のエラーメッセージが「入力してください。」で、
     * 項目名が「名前」の場合、生成されるメッセージは「[名前]入力してください。」となる。
     */
    private static class ViolationBasedStringResourceWithItemName implements StringResource {

        /** ロガー */
        private static final Logger LOGGER = LoggerManager.get(ViolationBasedStringResourceWithItemName.class);

        /** ID */
        private final String id;

        /** メッセージ */
        private final String message;

        /** 項目名を取得するためのメッセージID */
        private final String messageIdOfItemName;

        /**
         * 項目名付きメッセージを生成する。
         *
         * @param messageIdOfItemName 項目名のメッセージID
         * @param violation {@code ConstraintViolation}
         */
        public ViolationBasedStringResourceWithItemName(
                final String messageIdOfItemName, final ConstraintViolation<?> violation) {
            this.messageIdOfItemName = messageIdOfItemName;
            this.id = violation.getConstraintDescriptor()
                               .getAnnotation()
                               .annotationType()
                               .getName();
            this.message = violation.getMessage();
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getValue(final Locale locale) {
            String resultMessage = message;
            if (StringUtil.hasValue(messageIdOfItemName)) {
                try {
                    final String itemName = MessageUtil.getStringResource(messageIdOfItemName)
                                                       .getValue(locale);
                    resultMessage = '[' + itemName + ']' + message;
                } catch (MessageNotFoundException e) {
                    // メッセージが存在しない場合は何もしない。
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.logDebug("item name was not found in message resource.", e);
                    }
                }
            }
            return resultMessage;
        }
    }
}
