package org.goblintb;

import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SimpleReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {
    private final String key1;

    public SimpleReference(@NotNull PsiElement element, TextRange textRange) {
        super(element, textRange);
        key1 = element.getText().substring(element.getText().lastIndexOf("/") + 1, element.getText().length() - 1) + ".sql";
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        Project project = myElement.getProject();
        List<ResolveResult> results = new ArrayList<>();
        GlobalSearchScope scope = ModuleUtilCore.findModuleForPsiElement((PsiElement) myElement).getModuleContentWithDependenciesScope();
        @NotNull PsiFile[] filesByName = FilenameIndex.getFilesByName(project, key1, scope);
        if (filesByName != null) {
            for (final PsiFile psiFile : filesByName) {
                ResolveResult resolveResult = new ResolveResult() {
                    @Override
                    public @Nullable PsiElement getElement() {
                        return psiFile;
                    }

                    @Override
                    public boolean isValidResult() {
                        return true;
                    }
                };
                results.add(resolveResult);
            }
        }
        return results.toArray(new ResolveResult[0]);

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
