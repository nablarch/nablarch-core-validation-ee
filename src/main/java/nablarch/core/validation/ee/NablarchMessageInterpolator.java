package nablarch.core.validation.ee;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import javax.validation.MessageInterpolator;
import javax.validation.Validation;

import nablarch.core.message.Message;
import nablarch.core.message.MessageLevel;
import nablarch.core.message.MessageUtil;

/**
 * Nablarchのメッセージ管理機能を使用してメッセージを構築するクラス。
 * <p>
 * この実装では、メッセージの取得処理を{@link MessageUtil#createMessage(MessageLevel, String, Object...)}に委譲する。
 * {@link MessageUtil#createMessage(MessageLevel, String, Object...)}に指定するメッセージIDは以下のルールにより導出する。
 * <ol>
 * <li>メッセージが"{"、"}"で囲まれていてメッセージ内に"}"が存在しない場合は、前後のカッコを取り除いた値をメッセージIDとする。</li>
 * <li>上記以外の場合は、メッセージをデフォルトの{@link MessageInterpolator}によりメッセージに変換する。</li>
 * </ol>
 * <p>
 * 以下に例をしめす。
 * <pre>
 * <code>
 * // カッコが取り除かれ「user.required.message」がメッセージIDとなる。
 * {@literal @}Required(message = "{user.required.message}")
 * 
 * // 「{user.{required}.message}」をメッセージとして、
 * // デフォルトのMessageInterpolatorによりメッセージを構築する。
 * {@literal @}Required(message = "{user.{required}.message}")
 *
 * // 「入力してください。」をメッセージとして、
 * // デフォルトのMessageInterpolatorによりメッセージを構築する。
 * {@literal @}Required(message = "入力してください。")
 * </code>
 * </pre>
 *
 * @author Hisaaki Shioiri
 */
public class NablarchMessageInterpolator implements MessageInterpolator {

    /** メッセージIDの形式 */
    private static final Pattern MESSAGE_ID_PATTERN = Pattern.compile("^\\{[^\\}]+\\}$");

    /** デフォルトの{@link MessageInterpolator} */
    private final MessageInterpolator defaultMessageInterpolator = Validation.byDefaultProvider()
                                                                             .configure()
                                                                             .getDefaultMessageInterpolator();

    @Override
    public String interpolate(final String message, final Context context) {
        return interpolate(message, context, null);
    }

    @Override
    public String interpolate(final String message, final Context context, final Locale locale) {

        final Map<String, Object> options = context.getConstraintDescriptor()
                                                   .getAttributes();

        if (isMessageId(message)) {
            final Message m = MessageUtil.createMessage(MessageLevel.ERROR, getMessageId(message), options);
            return locale == null ? m.formatMessage() : m.formatMessage(locale);
        } else {
            return locale == null ?
                    defaultMessageInterpolator.interpolate(message, context) : defaultMessageInterpolator.interpolate(message, context, locale);
        }
    }

    /**
     * メッセージがメッセージID形式かどうか判定する。
     * <p>
     * メッセージが、"{"、"}"で囲まれている場合はメッセージID形式とする。
     * ただし、メッセージ中に"}"がある場合は、メッセージID形式とはならない。
     *
     * @param message メッセージ
     * @return メッセージID形式の場合{@code true}
     */
    private static boolean isMessageId(final String message) {
        return MESSAGE_ID_PATTERN.matcher(message)
                                 .matches();
    }

    /**
     * メッセージIDを取得する。
     * <p>
     * メッセージIDの前後の"{"、"}"を除去する。
     *
     * @param message メッセージ
     * @return メッセージID
     */
    private static String getMessageId(final String message) {
        return message.substring(1, message.length() - 1);
    }
}
