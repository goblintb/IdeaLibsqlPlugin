package org.goblintb;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.util.NotNullFactory;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.psi.*;
import com.intellij.psi.search.searches.AnnotatedElementsSearch;
import com.intellij.util.Query;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UastCallKind;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Test5 implements LineMarkerProvider {
    @Override
    public @Nullable LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {
        for (PsiElement element : elements) {
            if (element instanceof UCallExpression &&
                    ((UCallExpression) element).getKind().equals(UastCallKind.METHOD_CALL) &&
                    publicE((UCallExpression) element)) {
                PsiElement sourcePsi = ((UCallExpression) element).getSourcePsi();
                PsiElement identifier = ((UCallExpression) element).getMethodIdentifier().getSourcePsi();
                if (identifier != null && sourcePsi != null) {
                    NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder.create(AllIcons.General.OverridingMethod)
                            .setAlignment(GutterIconRenderer.Alignment.LEFT)
                            .setTargets(NotNullLazyValue.createValue((NotNullFactory<List<PsiElement>>) () -> find((UCallExpression) element)))
                            .setTooltipText("hiho");
                    result.add(builder.createLineMarkerInfo(identifier));
                }
        }
        }

    }

    private boolean publicE(UCallExpression element) {
        if (element.getMethodName() != null && element.getMethodName().equals("update")) {
            PsiMethod resolve = element.resolve();
            if (resolve != null) {
                PsiModifierList modifierList = resolve.getModifierList();
                return true;
            }
        }
        return false;
    }

    private List<PsiElement> find(UCallExpression element) {
        if (element.getValueArgumentCount() != 1){
            return Collections.emptyList();
        }
        PsiType expressionType = element.getValueArguments().get(0).getExpressionType();
        Module module = ModuleUtilCore.findModuleForPsiElement((PsiElement) element);


        JavaPsiFacade instance = JavaPsiFacade.getInstance(module.getProject());
        PsiClass eventListenerClass = instance.findClass("org.springframework.stereotype.Repository", module.getModuleWithDependenciesAndLibrariesScope(false));
        Query<PsiMethod> psiMethods = AnnotatedElementsSearch.searchPsiMethods(eventListenerClass, module.getModuleWithDependenciesAndLibrariesScope(false));
        List<PsiElement> list = new ArrayList<>();
        for (PsiMethod psiMethod : psiMethods) {
            list.add((PsiElement) psiMethod);
        }
        return list;

    }
}
