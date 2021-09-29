public class MainLevel {

    public static void main(String[] args) {
        Level2 level2 = new Level2();
//        level2.setField(43);
        System.out.println(level2.getField());
        System.out.println(level2.getFieldPub());
        String text = "ListeningTopicRemoteMatcherQuerySyncJob_999111_LISTENING,ListeningKeywordGroupRemoteMatcherQuerySyncJob_999111_LISTENING,ListeningThemeRemoteMatcherQuerySyncJob_999111_LISTENING";
        System.out.println("wassup");
        System.out.println(text.split(","));
        try {
            dilbar();
        } catch(Exception ex) {
            System.out.println("printing stack");
            ex.printStackTrace();
        }

//        System.out.println(level2.getField());
    }

    private static void dilbar() {

        System.out.println("1");
            System.out.println("janeman");
            janeman();


        System.out.println("2");
    }

    private static void janeman() {
        throw new IllegalArgumentException("wassup");
    }
}
