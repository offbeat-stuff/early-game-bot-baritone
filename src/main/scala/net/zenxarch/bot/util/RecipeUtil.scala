package net.zenxarch.bot.util

import scala.collection.mutable.HashMap
import net.minecraft.client.MinecraftClient
import net.minecraft.item.Item
import net.minecraft.recipe.BlastingRecipe
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.ShapedRecipe
import net.minecraft.recipe.SmeltingRecipe
import net.minecraft.recipe.SmokingRecipe

import net.zenxarch.bot.ZenBot.mc
import net.minecraft.item.BowItem
import scala.collection.mutable.ListBuffer

object RecipeUtil:
  private var craftingRecipes = new ListBuffer[ShapedRecipe]()
  private var blastingRecipes = new ListBuffer[BlastingRecipe]()
  private var smokingRecipes = new ListBuffer[SmokingRecipe]()
  private var smeltingRecipes = new ListBuffer[SmeltingRecipe]()

  def recache() =
    mc.getNetworkHandler()
      .getRecipeManager()
      .values()
      .forEach(r => {
        r match {
          case c: ShapedRecipe =>
            craftingRecipes += c
          case b: BlastingRecipe =>
            blastingRecipes += b
          case s: SmokingRecipe =>
            smokingRecipes += s
          case s: SmeltingRecipe =>
            smeltingRecipes += s
        }
      })

  def getIngredientItemIds(ing: Ingredient): List[Item] =
    return (for i <- ing.getMatchingStacks()
    yield i.getItem()).toList

  def simpleIngredient(r: Recipe[?]): HashMap[List[Item], Int] =
    var res = new HashMap[List[Item], Int]()
    r.getIngredients.forEach(r => {
      if !r.isEmpty() then
        val simple = getIngredientItemIds(r)
        res.put(simple, res.getOrElse(simple, 0) + 1)
    })
    return res

  def findBlastingRecipes(item: Item) = recipeFilter(blastingRecipes, item)

  def findSmokingRecipes(item: Item) = recipeFilter(smokingRecipes, item)

  def findSmeltingRecipes(item: Item) = recipeFilter(smeltingRecipes, item)

  def findCraftingRecipes(item: Item) = recipeFilter(craftingRecipes, item)

  private def recipeFilter[T <: Recipe[?]](
      in: ListBuffer[T],
      item: Item
  ): List[T] =
    return in.filter(_.getOutput().isOf(item)).toList
