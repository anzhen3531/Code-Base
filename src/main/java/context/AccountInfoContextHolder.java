package context;

/**
 * 线程隔离 使用的是JMM的机制
 */
public class AccountInfoContextHolder {
    /**
     * ThreadLocal 线程上下文 ，保存线程相关信息
     */
    private static final ThreadLocal<AccountInfoContext> contextHolder = new ThreadLocal<>();

    public static AccountInfoContext getContext() {
        return contextHolder.get();
    }

    public static void setContextHolder(AccountInfoContext context) {
        contextHolder.set(context);
    }

    public static ThreadLocal<AccountInfoContext> getContextHolder() {
        return contextHolder;
    }
}
