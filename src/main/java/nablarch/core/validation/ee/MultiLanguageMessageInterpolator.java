package nablarch.core.validation.ee;

import java.util.Locale;

import jakarta.validation.MessageInterpolator;

import nablarch.core.ThreadContext;
import nablarch.core.repository.SystemRepository;

/**
 * BeanValidationによるバリデーションエラー時のメッセージを{@link Locale}に応じて切り替えるためのMessageInterpolator.
 *
 * @author sumida
 * @author asato
 */
public class MultiLanguageMessageInterpolator implements MessageInterpolator {

    private static String MESSAGE_INTERPOLATOR_KEY = "messageInterpolator";

    /** バリデーションエラーメッセージの言語を切り替えるためのMessageInterpolator. */
    private MessageInterpolator messageInterpolator;
    

    /** コンストラクタ. */
    public MultiLanguageMessageInterpolator() {
        messageInterpolator = SystemRepository.get(MESSAGE_INTERPOLATOR_KEY);
        if (messageInterpolator == null) {
            messageInterpolator = new NablarchMessageInterpolator();
        }
    }

    /**
     * {@link Context}に基づいてメッセージテンプレートからメッセージを生成する。<br/>
     * 使用する{@link Locale}は、{@link ThreadContext}に設定されている場合はその値を使用し、
     * そうでない場合は{@link Locale#getDefault()}の値を使用する。
     *
     * @see jakarta.validation.MessageInterpolator#interpolate(java.lang.String,
     * jakarta.validation.MessageInterpolator.Context)
     */
    @Override
    public String interpolate(String messageKey, Context context) {
        return interpolate(messageKey, context, getLanguage());
    }

    /**
     * {@link Context}に基づいてメッセージテンプレートからメッセージを生成する。<br/>
     * @see jakarta.validation.MessageInterpolator#interpolate(java.lang.String, jakarta.validation.MessageInterpolator.Context, java.util.Locale)
     */
    @Override
    public String interpolate(String messageKey, Context context, Locale locale) {
        return messageInterpolator.interpolate(messageKey, context, locale);
    }

    /**
     * {@link Locale}を取得する。
     * @return {@link Locale}
     */
    private Locale getLanguage() {
        return ThreadContext.getLanguage() != null ? ThreadContext.getLanguage() : Locale.getDefault();
    }
}
