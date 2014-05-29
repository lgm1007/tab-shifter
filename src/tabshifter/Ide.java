package tabshifter;

import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.fileEditor.impl.EditorWindowHack;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.tabs.JBTabs;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

import static javax.swing.SwingUtilities.isDescendingFrom;

public class Ide {
    public final FileEditorManagerEx editorManager;
    private final VirtualFile currentFile;

    public Ide(FileEditorManagerEx editorManager, Project project) {
        this.editorManager = editorManager;
        this.currentFile = currentFileIn(project);
    }

    public EditorWindow createSplitter(int orientation) {
        editorManager.createSplitter(orientation, editorManager.getCurrentWindow());
        EditorWindow[] windows = editorManager.getWindows();
        return windows[windows.length - 1];
    }

    public void closeCurrentFileIn(Window window) {
        window.editorWindow.closeFile(currentFile);
    }

    public void openCurrentFileIn(Window window) {
        editorManager.openFileWithProviders(currentFile, true, window.editorWindow);
    }

    public void setFocusOn(Window window) {
        editorManager.setCurrentWindow(window.editorWindow);
    }

    public LayoutElement snapshotWindowLayout() {
        JPanel rootPanel = (JPanel) editorManager.getSplitters().getComponent(0);
        return snapshotWindowLayout(rootPanel);
    }

    private LayoutElement snapshotWindowLayout(JPanel panel) {
        Component component = panel.getComponent(0);

        if (component instanceof Splitter) {
            Splitter splitter = (Splitter) component;
            LayoutElement first = snapshotWindowLayout((JPanel) splitter.getFirstComponent());
            LayoutElement second = snapshotWindowLayout((JPanel) splitter.getSecondComponent());
            return new Split(first, second, !splitter.isVertical());

        } else if (component instanceof JPanel || component instanceof JBTabs) {
            EditorWindow editorWindow = findWindowWith(component);
            boolean hasOneTab = (editorWindow.getTabCount() == 1);
            boolean isCurrent = editorManager.getCurrentWindow().equals(editorWindow);
            return new Window(editorWindow, hasOneTab, isCurrent);

        } else {
            throw new IllegalStateException();
        }
    }

    private EditorWindow findWindowWith(Component component) {
        if (component == null) return null;

        for (EditorWindow window : editorManager.getWindows()) {
            if (isDescendingFrom(component, EditorWindowHack.panelOf(window))) {
                return window;
            }
        }
        return null;
    }

    private static VirtualFile currentFileIn(@NotNull Project project) {
        return ((FileEditorManagerEx) FileEditorManagerEx.getInstance(project)).getCurrentFile();
    }
}
