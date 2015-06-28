package com.youngsee.mirrorplayer.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class RuntimeExec {

    private static final String COMMAND_LINE_END = "\n";
    private static final String COMMAND_EXIT = "exit\n";
    private ProcessBuilder mPrcBuilder = new ProcessBuilder();

    private RuntimeExec() {
    }
    
    private static class RuntimeExecHolder {
        static final RuntimeExec INSTANCE = new RuntimeExec();
    }
    
    public synchronized static RuntimeExec getInstance() {
        return RuntimeExecHolder.INSTANCE;
    }
    
    public int runRootCmd(String command) {
        int exitVal = 0;
        Process process = null;

        try {
            synchronized (mPrcBuilder) {
                process = mPrcBuilder.command("su").redirectErrorStream(true).start();
            }
            
            if (process != null) {
                new ReadInputStream(process).start();
                excuteCmd(process.getOutputStream(), command);
                exitVal = process.waitFor();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (process.getOutputStream() != null) {
                    process.getOutputStream().close();
                }
                
                if (process.getInputStream() != null) {
                    process.getInputStream().close();
                }
                
                if (process.getErrorStream() != null) {
                    process.getErrorStream().close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        return exitVal;
    }

    
    private void excuteCmd(OutputStream osCmd, String cmd) {
        DataOutputStream dos = new DataOutputStream(osCmd);
        try {
            if (dos != null) {
                dos.writeBytes(cmd);
                dos.writeBytes(COMMAND_LINE_END);
                dos.flush();
                dos.writeBytes(COMMAND_EXIT);
                dos.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private final class ReadInputStream extends Thread {
        Process mProcess = null;
        
        ReadInputStream(Process process) {
            mProcess = process;
        }

        private int getCurrentProcessId() {
            if (mProcess != null) {
                String strProcess = mProcess.toString();
                try {
                    int i = strProcess.indexOf("=") + 1;
                    int j = strProcess.indexOf("]");
                    String cStr = strProcess.substring(i, j).trim();
                    return Integer.parseInt(cStr);
                } catch (Exception e) {
                    return 0;
                }
            }
            return 0;
        }
        
        private void readStream() {
            BufferedReader br = null;
            InputStreamReader isr = null;
            try {
                isr = new InputStreamReader(mProcess.getInputStream());
                br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    System.out.println("root:> " + line);
                }
            } catch (IOException ioe) {
            	ioe.printStackTrace();
            } finally {
                try {
                    if (br != null) {
                        br.close();
                    }
                    
                    if (isr != null) {
                        isr.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        public void run() {
            while (mProcess != null && getCurrentProcessId() != 0) {
                try {
                    mProcess.exitValue();
                    readStream();
                    break;
                } catch(IllegalThreadStateException e) {
                    readStream();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        break;
                    }
                }
            }
        }    
    }

}
