//
//  DetailViewController.h
//  maven-ios-example-project
//
//  Created by Sebastian Bott on 24.07.12.
//  Copyright (c) 2012 let's dev. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface DetailViewController : UIViewController <UISplitViewControllerDelegate>

@property (strong, nonatomic) id detailItem;

@property (strong, nonatomic) IBOutlet UILabel *detailDescriptionLabel;

@end
