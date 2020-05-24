package org.goblintb;

import com.google.common.base.Splitter;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimpleReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {

    public static List<String> CONST_URL = Arrays.asList("META-INF", "libsql", "queries");

    private final String rawString;
    /*
     * По этому пути будет искаться файл, начинается с CONST_URL
     */
    private final List<String> newList;
    public SimpleReference(@NotNull PsiElement element, TextRange textRange) {
        super(element, textRange);
        rawString = element.getText().substring(textRange.getStartOffset(), textRange.getEndOffset()) + ".sql";
        newList = new ArrayList<>(CONST_URL);
        newList.addAll(Splitter.on("/").splitToList(rawString));
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        Module module = ModuleUtilCore.findModuleForPsiElement(myElement);
        if (module != null) {
            ModuleRootManager instance = ModuleRootManager.getInstance(module);
            @NotNull VirtualFile[] sourceRoots = instance.getSourceRoots(false);
            for (VirtualFile root : sourceRoots) {
                String name = root.getName();
                if (!name.equals("resources")) {
                    continue;
                }
                PsiFile byPath = findByPath(0, newList.size(), newList, myElement.getProject(), root);
                return new ResolveResult[]{new ResolveResult() {
                    @Override
                    public @Nullable PsiElement getElement() {
                        return byPath;
                    }

                    @Override
                    public boolean isValidResult() {
                        return true;
                    }
                }};
            }
        }
        return new ResolveResult[0];
    }

    /**
     * Рекурсивный метод, ищет по переданным параметрам детей по структуре файлов
     * @param index для рекурсии, глубина, начанется с 1
     * @param size для рекурсии, размер глубины рекурсии
     * @param path массив с поиском, обычно в нём массив вида ["META-INF", "libsql", "queries", "search", "ContractSearch.sql"]
     * @param project проект, нужен для физического поиска директории.
     * @param parent это элемент, дети которого будут проверяться
     * @return найденный файл.
     */
    private PsiFile findByPath(int index, int size, List<String> path, Project project, VirtualFile parent) {
        //последний аргумент хранит файл
        if (index == size - 1) {
            PsiDirectory directory = PsiManager.getInstance(project).findDirectory(parent);
            if (directory != null) {
                return directory.findFile(path.get(index));
            } else {
                return null;
            }
        }
        for (VirtualFile resourcesParent : parent.getChildren()) {
            if (resourcesParent.getName().equals(path.get(index))) {
                return findByPath(index + 1, size, path, project, resourcesParent);
            }
        }
        return null;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        ResolveResult[] resolveResults = multiResolve(false);
        return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new ArrayList<>().toArray();
    }
}
