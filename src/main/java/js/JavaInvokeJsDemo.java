package js;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class JavaInvokeJsDemo {

    public static void main(String[] args) {
        // 构造一个脚本引擎管理器
        ScriptEngineManager manager = new ScriptEngineManager();
        // 遍历所有的引擎工厂，输出引擎工厂的信息
        for (ScriptEngineFactory factory : manager.getEngineFactories()) {
            String engineName = factory.getEngineName();
            String engineVersion = factory.getEngineVersion();
            String languageName = factory.getLanguageName();
            String languageVersion = factory.getLanguageVersion();
            ScriptEngine engine = factory.getScriptEngine();
            System.out.println(String.format("引擎名称：%s\t引擎版本：%s\t语言名称：%s\t语言版本：%s\t",
                    engineName, engineVersion, languageName, languageVersion));
            // 如果支持JavaScript
            if ("ECMAScript".equals(languageName)) {
                callSimpleJavaScript(engine);
                //callJavaScriptFromFile(engine);
            }
        }
    }

    /**
     * 从简单字符串执行JavaScript脚本
     *
     * @param engine 脚本引擎
     */
    private static void callSimpleJavaScript(ScriptEngine engine) {
        try {
            final String script = "var rowData = '36.8,zhangsan,22';\n" +
                    "var dataArray = rowData.split(\",\");\n" +
                    "var json = {};json.age=dataArray[2];\n" +
                    "json.name=dataArray[1];\n" +
                    "json.temperature=dataArray[0];\n" +
                    "JSON.stringify(json);";
//            final String script1 = "var rowData = '36.8,zhangsan,22';";
//            final String script2 = "var dataArray = rowData.split(\",\");";
//            final String script3 = "var json = {};json.age=dataArray[2];";
//            final String script4 = "json.name=dataArray[1];";
//            final String script5 = "json.temperature=dataArray[0]";
//            final String script6 = "JSON.stringify(json);";
//            System.out.println(script1 + " 的执行结果是：" + engine.eval(script1));
//            System.out.println(script2 + " 的执行结果是：" + engine.eval(script2));
//            System.out.println(script3 + " 的执行结果是：" + engine.eval(script3));
//            System.out.println(script4 + " 的执行结果是：" + engine.eval(script4));
//            System.out.println(script5 + " 的执行结果是：" + engine.eval(script5));
//            System.out.println(script6 + " 的执行结果是：" + engine.eval(script6));
            System.out.println(script + " 的执行结果是：" + engine.eval(script));
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从JavaScript文件执行JavaScript脚本
     * @param engine 脚本引擎
     */
    private static void callJavaScriptFromFile(ScriptEngine engine) {
        try {
            final String fileName = "D:/test.js";
            File file = new File(fileName);
            if (file.exists()) {
                System.out.println("从 " + fileName + " 的执行结果是：" + engine.eval(new FileReader(file)));
            } else {
                System.err.println(fileName + " 不存在，无法执行脚本");
            }
        } catch (ScriptException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
