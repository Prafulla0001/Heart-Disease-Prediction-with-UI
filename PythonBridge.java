import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PythonBridge {
    private static String pythonExecutable = null;

    private static boolean isWorkingPython(String path) {
        try {
            Process p = new ProcessBuilder(path, "--version").start();
            boolean finished = p.waitFor(3, TimeUnit.SECONDS);
            return finished && p.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static synchronized String getPythonPath() {
        if (pythonExecutable != null) {
            return pythonExecutable;
        }

        // Candidates in order of preference
        String[] candidates = new String[]{
                ".venv\\Scripts\\python.exe",
                ".venv/bin/python",
                "python",
                "python3",
                "py"
        };

        for (String candidate : candidates) {
            if (candidate.contains(File.separator) || candidate.contains("/") || candidate.contains("\\")) {
                File f = new File(candidate);
                if (f.exists() && isWorkingPython(f.getAbsolutePath())) {
                    pythonExecutable = f.getAbsolutePath();
                    return pythonExecutable;
                }
            } else {
                if (isWorkingPython(candidate)) {
                    pythonExecutable = candidate;
                    return pythonExecutable;
                }
            }
        }

        // Default fallback
        pythonExecutable = "python";
        return pythonExecutable;
    }

    public static ExecutionResult runScript(String scriptPath, String... args) {
        List<String> command = new ArrayList<>();
        command.add(getPythonPath());
        command.add(scriptPath);
        command.addAll(Arrays.asList(args));

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(new File("."));

        StringBuilder stdout = new StringBuilder();
        StringBuilder stderr = new StringBuilder();

        try {
            Process process = pb.start();

            // Read standard output
            Thread outThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stdout.append(line).append("\n");
                    }
                } catch (Exception e) {
                    stdout.append("[Error reading stdout: ").append(e.getMessage()).append("]");
                }
            });

            // Read error output
            Thread errThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stderr.append(line).append("\n");
                    }
                } catch (Exception e) {
                    stderr.append("[Error reading stderr: ").append(e.getMessage()).append("]");
                }
            });

            outThread.start();
            errThread.start();

            boolean finished = process.waitFor(15, TimeUnit.SECONDS);
            outThread.join(2000);
            errThread.join(2000);

            if (!finished) {
                process.destroyForcibly();
                return new ExecutionResult(-1, stdout.toString().trim(), "Process timed out after 15 seconds.");
            }

            return new ExecutionResult(process.exitValue(), stdout.toString().trim(), stderr.toString().trim());
        } catch (Exception ex) {
            return new ExecutionResult(-1, stdout.toString().trim(), "Execution exception: " + ex.getMessage());
        }
    }

    public static class ExecutionResult {
        private final int exitCode;
        private final String output;
        private final String error;

        public ExecutionResult(int exitCode, String output, String error) {
            this.exitCode = exitCode;
            this.output = output;
            this.error = error;
        }

        public boolean isSuccess() {
            return exitCode == 0;
        }

        public int getExitCode() {
            return exitCode;
        }

        public String getOutput() {
            return output;
        }

        public String getError() {
            return error;
        }
    }

    public static void main(String[] args) {
        System.out.println("Testing PythonBridge...");
        System.out.println("Detected Working Python: " + getPythonPath());
        ExecutionResult res = runScript("predict_heart.py", "52", "1", "0", "125", "212", "0", "1", "168", "0", "1", "2", "2", "3");
        System.out.println("Exit Code: " + res.getExitCode());
        System.out.println("Output: " + res.getOutput());
        if (!res.getError().isEmpty()) {
            System.out.println("Error: " + res.getError());
        }
    }
}